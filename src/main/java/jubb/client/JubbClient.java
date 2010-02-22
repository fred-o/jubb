package jubb.client;

public interface JubbClient {

	/**
	 * Post a job in serialized form to the queue.
	 */
	public void post(int priority, String data);

	/**
	 * Post a job to the queue, using JSON as serializtion method.
	 */
	public void post(int priority, Object data);

}