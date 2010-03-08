package jubb.client;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncJubbClientImpl implements AsyncJubbClient {
	private static ExecutorService POOL = Executors.newCachedThreadPool();
	private DirectJubbClient _client;

	public AsyncJubbClientImpl(URI uri) {
		this._client = new DirectJubbClient(uri);
	}

	private <T> Runnable createAddTask(final T data) {
		return new Runnable() {
			public void run() {
				_client.add(data);
			}
		};
	}

	public void add(String data) {
		POOL.submit(createAddTask(data));
	}
    
	public void add(Object data) {
		POOL.submit(createAddTask(data));
	}

	public void take(final Callback<InputStream> callback) {
		POOL.submit(new Runnable() {
				public void run() {
					callback.invoke(_client.take());
				}
			});
	}
	
	public <T> void take(final Class<T> clazz, final Callback<T> callback) {
		POOL.submit(new Runnable() {
				public void run() {
					callback.invoke(_client.take(clazz));
				}
			});
	}
}