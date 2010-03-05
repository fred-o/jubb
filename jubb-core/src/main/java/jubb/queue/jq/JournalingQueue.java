package jubb.queue.jq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

import jubb.queue.JubbQueue;
import jubb.queue.jq.JournalingQueue;

/**
 * This is a simple persistant queue implementation that uses a
 * journaling scheme based on Java serialization. It does not support
 * different priorities for jobs.
 */
public class JournalingQueue implements JubbQueue {
	private static final Logger LOG = Logger.getLogger(JournalingQueue.class);
	private BlockingQueue<Job> _queue;
	private File dir;
	private File current;
	private List<File> markedForDelete = new LinkedList<File>();
	private ObjectOutputStream out;
	private BlockingQueue<JournalingQueue.Job> queue;
	private int recordsWritten = 0;
	private Executor executor = Executors.newSingleThreadExecutor();

	public JournalingQueue(File dir) throws IOException {
		this.dir = dir;
		if (!dir.exists()) dir.mkdirs();
		this._queue = restoreQueue();

		snapshot();

		Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					close();
				}
			});
	}

	public void add(String data) {
		Job job = new Job(System.currentTimeMillis(), data);
		this.executor.execute(new RecordWriter(new Record(true, job)));
		this._queue.add(job);
		System.out.println("ADD. " + _queue);
	}

	public String take() throws InterruptedException {
		Job job = this._queue.take();
		this.executor.execute(new RecordWriter(new Record(false, null)));
		return job.data;
	}

	public String poll() {
		Job job = this._queue.poll();
		if (job != null) {
			this.executor.execute(new RecordWriter(new Record(false, null)));
			return job.data;
		} 
		return null;
	}

	public int size() {
		return _queue.size();
	}
	
	public void close() {
		try {
			if(out != null) {
				out.flush();
				out.close();
			}
		}
		catch (IOException ioe) {
			LOG.error("Error closing journal: ", ioe);
		}
	}
	
	protected BlockingQueue<Job> restoreQueue() {
		BlockingQueue<JournalingQueue.Job> q = new LinkedBlockingQueue<JournalingQueue.Job>();
		try {
			File f = mostCurrentFile();
			if (f != null) {
				ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
				try {
					q = (BlockingQueue<JournalingQueue.Job>) in.readObject();
					while (true) {
						Record r = (Record) in.readObject();
						if (r.add) {
							q.add(r.job);
						} else {
							q.remove();
						} 
					}
				} finally {
					in.close();
				}
			} 
		} catch (ClassNotFoundException cnfe) {
			LOG.error("Could not restore job queue from journal", cnfe);
		} catch (IOException ioe) {
			LOG.error("Could not restore job queue from journal", ioe);
		}
		try {
		    deleteMarkedFiles();
		}
		catch (IOException ioe) {
			LOG.error("Error deleting stale log files", ioe);
		}
		System.out.println("Restored queue with " + q.size() + " jobs");
		System.out.println(q);
		return q;
	}

	protected void markForDeletion(File f) {
		if(f != null)
			this.markedForDelete.add(f);
	}

	protected void deleteMarkedFiles() throws IOException {
		for(File f: this.markedForDelete) {
			System.out.println("DELETING " + f.getAbsolutePath());
		    f.delete();
		}
		this.markedForDelete.clear();
	}
	
	protected File nextFile() throws IOException {
		markForDeletion(this.current);
		this.current = File.createTempFile("jubb", ".jq", dir);
		return this.current;
	}

	protected File mostCurrentFile() throws IOException {
		File[] files = this.dir.listFiles();
		if (files.length > 0) {
			Arrays.sort(files, new Comparator<File>() {
						public int compare(File f1, File f2) {
							return (int)(f2.lastModified() - f1.lastModified());
						}
					});
			for(int i = 1; i < files.length; i++) {
			    markForDeletion(files[i]);
			}
			return files[0];
		}		
		return null;
	}

	private void nextOutputStream() throws IOException {
		close();
		File f = nextFile();
		this.out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f, true)));
	}

	protected void snapshot() {
		try {
			System.out.println("SNAPSHOT!");
			nextOutputStream();
			this.out.writeObject(_queue);
			this.out.flush();
			deleteMarkedFiles();
		}
		catch (IOException ioe) {
			LOG.error("Couldn't create snapshot", ioe);
		}
	}

	class RecordWriter implements Runnable {
		private Record rec;

		public RecordWriter(Record rec) {
			this.rec = rec;
		}

		public void run() {
			try {
				try {
					out.writeObject(rec);
				} finally {
					out.flush();
				}
				if(++recordsWritten > 100) {
					snapshot();
					recordsWritten = 0;
				}
			} catch (IOException ioe) {
				LOG.error("Error writing to log file", ioe);
			}
		}
	}

	static class Record implements Serializable {
		public final boolean add;
		public final JournalingQueue.Job job;

		public Record(boolean add, JournalingQueue.Job job) {
			this.add = add;
			this.job = job;
		}
	}		
	
	static class Job implements Serializable {
		final long timestamp;
		final String data;

		public Job(long timestamp, String data) {
			if (data == null) 
				throw new IllegalArgumentException("data cannot be null");
			this.timestamp = timestamp;
			this.data = data;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Job))
				return false;
			Job other = (Job) obj;
			if (this.timestamp != other.timestamp) 
				return false;
			return this.data.equals(other.data);
		}

		@Override
		public String toString() {
			return "[time=" + timestamp + ",data="+ data + "]";
		}
	}
	
}