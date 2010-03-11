package jubb.client;

import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

public class AsyncJubbClientImpl implements AsyncJubbClient {
	private static ExecutorService POOL = Executors.newCachedThreadPool();
	private DirectJubbClient _client;

	public AsyncJubbClientImpl(URI uri) {
		HttpParams params = new BasicHttpParams();
		ConnManagerParams.setMaxTotalConnections(params, 20);
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		this._client = new DirectJubbClient(uri, new DefaultHttpClient(cm, params));
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