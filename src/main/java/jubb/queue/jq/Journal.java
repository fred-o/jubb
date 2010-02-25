package jubb.queue.jq;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

public class Journal {
	private static final Logger LOG = Logger.getLogger(Journal.class);
	private File dir;

	private File current;
	private BufferedOutputStream out;

	public Journal(File dir) throws IOException {
		this.dir = dir;
		next();

		Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					try {
						if(out != null) out.close();
					}
					catch (IOException ioe) {
						LOG.error("Error closing journal: ", ioe);
					}
				}
			});
	}

	private void next() throws IOException {
		// TODO: implement actual algorithm
		if (out != null) {
			out.close();
		}
		this.current = new File(dir, "0000001.log");
		this.out = new BufferedOutputStream(new FileOutputStream(this.current));
	}



}