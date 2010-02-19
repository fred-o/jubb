package web.shrimp.client.event.client;

public interface JubbClient {

	/**
	 * Post a job to the queue.
	 */
	public void post(Priority priority, Object data);

	/**
	 * Take the first job off a queue and mark it as 'in progress'.
	 */
	public Job start();

	/**
	 * Mark the given job 'finished'.
	 */
	public void finish(Job job);

	/**
	 * Functionally equvalent to an atomic finishJob(startJob()) operation.
	 */
	public Job take();

	public static class Job {
		final int id;
		final Object data;
	}
}