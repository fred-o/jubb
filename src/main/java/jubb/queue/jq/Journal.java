package jubb.queue.jq;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import jubb.queue.JubbQueue;

public class Journal {
	private static final Logger LOG = Logger.getLogger(Journal.class);
	private File dir;
	private File current;
	private ObjectOutputStream out;
	private ObjectInputStream in;

	public Journal(File dir) throws IOException {
		this.dir = dir;

		Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					close();
				}
			});
	}

	private void nextFile() throws IOException {
		// TODO: implement actual algorithm
		this.current = new File(dir, "0000001.log");
	}

	private void nextOutputStream() throws IOException {
		if (out != null) {
			out.close();
		}
		nextFile();
		this.out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.current)));
	}

	private void nextInputStream() throws IOException {
		nextFile();
		this.in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(this.current)));
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

	private void appendRecord(int op, JournalingQueue.Job job) {
		try {
			if (out == null)
				nextOutputStream();
			
			try {
				this.out.writeInt(op);
				this.out.writeInt(job.priority);
				this.out.writeLong(job.timestamp);
				byte[] buf = job.data.getBytes();
				this.out.writeInt(buf.length);
				this.out.write(buf);
			} finally {
				this.out.flush();
			}
		} catch (IOException ioe) {
			LOG.error("Error writing to log file", ioe);
		}
	}

	public void appendAdd(JournalingQueue.Job job) {
		appendRecord(1, job);
	}

	public void appendRemove(JournalingQueue.Job job) {
		appendRecord(2, job);
	}

	private Record readRecord() throws IOException {
		int op = this.in.readInt();
		int priority = this.in.readInt();
		long timestamp = this.in.readLong();
		int len = this.in.readInt();
		byte[] buf = new byte[len];
		int r = this.in.read(buf, 0, len); 
		if (r != len) 
			throw new IOException("Expected " + len + " bytes but got " + r);
		return new Record(op, new JournalingQueue.Job(priority, timestamp, new String(buf)));
	}

	public PriorityBlockingQueue<JournalingQueue.Job> restore() {
		PriorityBlockingQueue<JournalingQueue.Job> q = new PriorityBlockingQueue<JournalingQueue.Job>();
		try {
			nextInputStream();
			try {
//					while (in.available() > 0) {
				while (true) {
					Record r = readRecord();
					if (r.op == 1) {
						q.add(r.job);
					} else if (r.op == 2) {
						q.remove(r.job);
					} 
				}
			} finally {
				in.close();
			}
		} catch (IOException ioe) {
			LOG.error("Could not restore job queue from journal", ioe);
		}
		System.out.println("Restored queue with " + q.size() + " jobs");
		System.out.println(q);
		
		return q;
	}

	static class Record {
		public final int op;
		public final JournalingQueue.Job job;

		public Record(int op, JournalingQueue.Job job) {
			this.op = op;
			this.job = job;
		}
	}		

}