package jubb.queue.jq;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;

public class JournalInput extends AbstractJournalAccess {
    private static final Logger LOG = Logger.getLogger(JournalInput.class);
	private ObjectInputStream in;

	public JournalInput(File dir) {
		super(dir);
	}

	public void close() {
		try {
		    in.close();
		}
		catch (IOException ioe) {
			LOG.error("Error closing input", ioe);
		}
	}

	private void nextInputStream() throws IOException {
		File f = nextFile();
		this.in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));
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
	
}