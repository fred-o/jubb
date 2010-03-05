package jubb.client;

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

}