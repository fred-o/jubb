package jubb.queue.jq;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.apache.log4j.Logger;

public class JournalOutput extends AbstractJournalAccess {
    private static final Logger LOG = Logger.getLogger(JournalOutput.class);
	private ObjectOutputStream out;

	public JournalOutput(File dir) {
		super(dir);

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

}