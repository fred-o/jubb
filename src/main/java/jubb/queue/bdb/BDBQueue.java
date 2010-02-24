package jubb.queue.bdb;

import java.io.File;

import jubb.queue.JubbQueue;

/**
 * {@link JubbQueue} implementation backed by a BerkleyDB instance.
 */
public class BDBQueue implements JubbQueue {

	public BDBQueue(File store) {
		
	}
    
	public void add(int priority, String data) {

	}

	public String take() {
		return null;
	}

	public String poll() {
		return null;
	}

	public int size() {
		return 0;
	}

}