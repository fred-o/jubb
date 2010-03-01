package jubb.queue.jq;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public abstract class AbstractJournalAccess {
	private File dir;
	private File current;
	private List<File> markedForDelete = new LinkedList<File>();

	public AbstractJournalAccess(File dir) throws IOException {
		this.dir = dir;
		this.current = mostCurrentFile();
	}

	protected void markForDeletion(File f) {
		this.markedForDelete.add(f);
	}

	protected void deleteMarkedFiles() throws IOException {
		for(File f: this.markedForDelete) {
			System.out.println("DELETING " + f.getAbsolutePath());
		    f.delete();
		}
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