package jubb.client;

import java.io.InputStream;

import jubb.client.AsyncJubbClient;
import jubb.client.DirectJubbClient;

/**
 * This class is a little more sophisticated than {@link
 * DirectJubbClient}; jobs are posted asynchronously in a background
 * thread, and failed jobs are put on a 'retry' queue for later delivery.
 */
public interface AsyncJubbClient {

	public void add(String data);

	public void add(Object data);

	public void take(Callback<InputStream> call);
	
	public <T> void take(Class<T> clazz, Callback<T> call);
	
}