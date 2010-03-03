package jubb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;

import jubb.client.DirectJubbClient;
import jubb.client.JubbConsumer;

/**
 * This simple implementation connects to a Jubb queue and posts its
 * jobs synchronusly. There are no facilities for retrying failed posts.
 */
public class DirectJubbClient implements JubbClient, JubbConsumer {
	private static final Logger LOG = Logger.getLogger(DirectJubbClient.class);
	private HttpClient httpClient = new DefaultHttpClient();
	private ObjectMapper mapper = new ObjectMapper();

	private HttpHost host;
	private String path;

	public DirectJubbClient(URL url) {
		this.host = new HttpHost(url.getHost());
		this.path = url.getPath();
	}

	/**
	 * @throws 
	 */
	public void post(int priority, String data) {
		_invoke("post", data, EMPTY);
	}

	public void post(int priority, Object data) {
		try {
			StringWriter w = new StringWriter();
			mapper.writeValue(w, data);
			post(priority, w.toString());
		} catch (JsonGenerationException jge) {
			jge.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private <C> C _invoke(String op, Call<C> callback) {
		return _invoke(op, null, callback);
	}

	/**
	 * General method for accessing the queue and getting a job object
	 * back.
	 */
	private <C> C _invoke(String op, String data, Call<C> callback) {
		try {
			HttpPost call = new HttpPost(path);
			call.getParams().setParameter("op", op).setParameter("data", data);
			HttpResponse res = httpClient.execute(call);
			if (res.getStatusLine().getStatusCode() == 200) {
				InputStream in = res.getEntity().getContent();
				try {
					return callback.call(in);
				} finally {
					in.close();
				}
			} 
		} catch (IOException ioe) {
			LOG.error("Error calling '" + op + "' on queue at " + path, ioe);
		}
		return null;
	}

	private static Call<Void> EMPTY = new Call<Void>() {
		public Void call(InputStream in) {
			return null;
		}
	};

	private static Call<InputStream> STREAM = new Call<InputStream>() {
		public InputStream call(InputStream in) {
			return in;
		}
	};

	private <T> Call<T> createJsonCall(final Class<T> clazz) {
		return new Call<T>() {
			public T call(InputStream in) throws IOException {
				return mapper.readValue(in, clazz);
			}
		};
	}

	public InputStream poll() {
		return _invoke("poll", STREAM);
	}

	public InputStream take() {
		return _invoke("take", STREAM);
	}

	public <T> T poll(Class<T> clazz) {
		return _invoke("poll", createJsonCall(clazz));
	}

	public <T> T take(final Class<T> clazz) {
		return _invoke("take", createJsonCall(clazz));
	}

	static interface Call<C> {
		public C call(InputStream in) throws IOException;
	}
}