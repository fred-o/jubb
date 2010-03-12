package jubb.queue;


public interface JubbQueue {

	public void add(Job job);

	/**
	 * Remove the first object from the queue, returning null if empty.
	 */
	public Job poll();

	/**
	 * Remove the first object from the queue, waiting for input if the queue is empty.
	 */
	public Job take() throws InterruptedException;

	public int size();

}