package jubb.client;

import java.io.InputStream;

public interface JubbClient {

	/**
	 * Post a job in serialized form to the queue.
	 */
	public void add(String data);

	/**
	 * Post a job to the queue, using JSON as serializtion method.
	 */
	public void add(Object data);

	/**
	 * Take a job in non-serialized form from the queue, returning
	 * null if no jobs are available.
	 */
	public InputStream poll();

	/**
	 * Take a job from the queue, returning null if no jobs are
	 * available.
	 */
	public <T> T poll(Class<T> clazz);

	/**
	 * Take a job in non-serialized form from the queue, blocking
	 * until one becomes avaiable if necessary.
	 */
	public InputStream take();

	/**
	 * Take a job from the queue, blocking until one becomes available
	 * if necessary.
	 */
	public <T> T take(Class<T> clazz);

}