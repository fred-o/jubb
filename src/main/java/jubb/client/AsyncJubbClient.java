package jubb.client;

import java.net.URL;

import jubb.client.AsyncJubbClient;
import jubb.client.DirectJubbClient;

/**
 * This class is a little more sophisticated than {@link
 * DirectJubbClient}; jobs are posted asynchronously in a background
 * thread, and failed jobs are put on a 'retry' queue for later delivery.
 */
public class AsyncJubbClient implements JubbClient {
	private DirectJubbClient _client;

	public AsyncJubbClient(URL url) {
		this._client = new DirectJubbClient(url);
	}

	public void post(int priority, String data) {
		this._client.post(priority, data);
	}
    
	public void post(int priority, Object data) {
		this._client.post(priority, data);
	}

}