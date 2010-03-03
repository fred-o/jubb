package jubb.client;


import java.io.InputStream;

public interface JubbConsumer {

	public InputStream poll();

	/**
	 * Take a job in non-serialized form from the queue, blocking
	 * until one becomes avaiable if necessary.
	 */
	public InputStream take();

	/**
	 * 
	 */
	public <T> T poll(Class<T> clazz);

	/**
	 * Take a job from the queue, blocking until one becomes available
	 * if necessary.
	 */
	public <T> T take(Class<T> clazz);

}