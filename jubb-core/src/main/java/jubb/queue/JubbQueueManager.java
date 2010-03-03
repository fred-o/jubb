package jubb.queue;

import java.io.IOException;
import java.util.Iterator;

import jubb.queue.JubbQueue;

public interface JubbQueueManager {
	
	/**
	 * Return the named queue, or null if none exists.
	 */
	public JubbQueue getQueue(String name);

	/**
	 * Create the named queue.
	 */
	public void createQueue(String name) throws IOException;

	
	public Iterator<String> getQueueNames();
	

}