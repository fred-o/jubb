package jubb.queue;


public interface JubbQueue {

	/**
	 * Add a job to the end of the queue.
	 */
	public void add(Job job);

	/**
	 * Remove the first object from the queue, returning null if empty.
	 */
	public Job poll();

	/**
	 * Remove the first object from the queue, waiting for input if the queue is empty.
	 */
	public Job take() throws InterruptedException;

	/**
	 * Return the number of jobs queued.
	 */
	public int getSize();

	/**
	 * Describe what type of queue this is.
	 */
	public String getType();

}