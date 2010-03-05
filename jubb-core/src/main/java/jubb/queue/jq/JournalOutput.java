package jubb.queue.jq;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import jubb.queue.jq.JournalingQueue;

public class JournalOutput extends AbstractJournalAccess {
    private static final Logger LOG = Logger.getLogger(JournalOutput.class);
	private ObjectOutputStream out;

	public JournalOutput(File dir, BlockingQueue<JournalingQueue.Job> q) throws IOException {
		super(dir);
		snapshot(q);
		Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					close();
				}
			});
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

	private void nextOutputStream() throws IOException {
		close();
		File f = nextFile();
		this.out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f, true)));
	}

	private void appendRecord(boolean add, JournalingQueue.Job job) {
		try {
			try {
				this.out.writeObject(new Record(add, job));
 			} finally {
				this.out.flush();
			}
		} catch (IOException ioe) {
			LOG.error("Error writing to log file", ioe);
		}
	}

	public void appendAdd(JournalingQueue.Job job) {
		appendRecord(true, job);
	}

	public void appendRemove(JournalingQueue.Job job) {
		appendRecord(false, job);
	}

	public void snapshot(BlockingQueue<JournalingQueue.Job> q) {
		try {
			System.out.println("SNAPSHOT!");
			nextOutputStream();
			this.out.writeObject(q);
			this.out.flush();
			deleteMarkedFiles();
		}
		catch (IOException ioe) {
		}
	}

}