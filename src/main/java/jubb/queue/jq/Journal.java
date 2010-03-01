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
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

import com.sleepycat.je.rep.util.ldiff.Record;

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
		this.out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(this.current, true)));
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
				this.out.writeObject(new Record(op, job));
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
		try {
			return (Record) this.in.readObject();
		} catch (ClassNotFoundException cnfe) {
			throw new IOException(cnfe);
		}
	}

	public PriorityBlockingQueue<JournalingQueue.Job> restore() {
		PriorityBlockingQueue<JournalingQueue.Job> q = new PriorityBlockingQueue<JournalingQueue.Job>();
		try {
			nextInputStream();
			try {
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

	static class Record implements Serializable {
		public final int op;
		public final JournalingQueue.Job job;

		public Record(int op, JournalingQueue.Job job) {
			this.op = op;
			this.job = job;
		}
	}		

}