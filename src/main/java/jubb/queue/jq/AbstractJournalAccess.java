package jubb.queue.jq;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public abstract class AbstractJournalAccess {
	private File dir;
	private File current;

	public AbstractJournalAccess(File dir) {
		this.dir = dir;
	}

	protected File nextFile() throws IOException {
		// TODO: implement actual algorithm
		this.current = new File(dir, "0000001.log");
		return current;
	}

	public abstract void close();

	static class Record implements Serializable {
		public final int op;
		public final JournalingQueue.Job job;

		public Record(int op, JournalingQueue.Job job) {
			this.op = op;
			this.job = job;
		}
	}		

}