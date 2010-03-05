package jubb.client;

import java.io.InputStream;
import java.net.URI;

import jubb.client.AsyncJubbClient;
import jubb.client.DirectJubbClient;

/**
 * This class is a little more sophisticated than {@link
 * DirectJubbClient}; jobs are posted asynchronously in a background
 * thread, and failed jobs are put on a 'retry' queue for later delivery.
 */
public class AsyncJubbClient implements JubbClient {
	private DirectJubbClient _client;

	public AsyncJubbClient(URI uri) {
		this._client = new DirectJubbClient(uri);
	}

	public void add(String data) {
		this._client.add(data);
	}
    
	public void add(Object data) {
		this._client.add(data);
	}

	public InputStream poll() {
		return this._client.poll();
	}

	public <T> T poll(Class<T> clazz) {
		return this._client.poll(clazz);
	}

	public InputStream take() {
		return this._client.take();
	}

	public <T> T take(Class<T> clazz) {
		return this._client.take(clazz);
	}

}