package jubb.client;

public interface JubbConsumer {
    
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

}