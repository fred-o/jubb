package jubb.queue;


public interface JubbQueue {

	public void add(String data);

	/**
	 * Remove the first object from the queue, returning null if empty.
	 */
	public String poll();

	/**
	 * Remove the first object from the queue, waiting for input if the queue is empty.
	 */
	public String take() throws InterruptedException;

	public int size();

}