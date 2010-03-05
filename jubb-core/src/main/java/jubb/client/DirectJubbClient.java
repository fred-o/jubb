package jubb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;

import jubb.client.DirectJubbClient;
import jubb.client.JubbConsumer;

/**
 * This simple implementation connects to a Jubb queue and posts its
 * jobs synchronusly. There are no facilities for retrying failed posts.
 */
public class DirectJubbClient implements JubbClient {
	private static final Logger LOG = Logger.getLogger(DirectJubbClient.class);
	private HttpClient httpClient = new DefaultHttpClient();
	private ObjectMapper mapper = new ObjectMapper();

	private URI uri;

	public DirectJubbClient(URI uri) {
		this.uri = uri;
	}

	/**
	 * General method for accessing the queue and getting a job object
	 * back.
	 */
	private <C> C _invoke(String op, String data, Call<C> callback, boolean close) {
		try {
			HttpPost call = new HttpPost(uri);
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
				Arrays.asList(
					new BasicNameValuePair("op", op), 
					new BasicNameValuePair("data", data)));
			call.setEntity(entity);
			call.getParams().setIntParameter("http.connection.timeout", 10000);

			HttpResponse res = httpClient.execute(call);
			if (res.getStatusLine().getStatusCode() == 200) {
				InputStream in = res.getEntity().getContent();
				try {
					return callback.call(in);
				} finally {
					if (close)
						in.close();
				}
			} else {
				LOG.error("Calling '" + op + "' on queue at '" + uri + "' failed with reason: '" + res.getStatusLine().toString());
			}
		} catch (IOException ioe) {
			LOG.error("Error calling '" + op + "' on queue at " + uri, ioe);
		}
		return null;
	}

	/**
	 * @throws 
	 */
	public void add(String data) {
		_invoke("add", data, VOID, true);
	}

	public void add(Object data) {
		try {
			StringWriter w = new StringWriter();
			mapper.writeValue(w, data);
			add(w.toString());
		} catch (JsonGenerationException jge) {
			jge.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private <C> C _invoke(String op, Call<C> callback, boolean close) {
		return _invoke(op, null, callback, close);
	}

	private static Call<Void> VOID = new Call<Void>() {
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
		return _invoke("poll", STREAM, false);
	}

	public InputStream take() {
		return _invoke("take", STREAM, false);
	}

	public <T> T poll(Class<T> clazz) {
		return _invoke("poll", createJsonCall(clazz), true);
	}

	public <T> T take(final Class<T> clazz) {
		return _invoke("take", createJsonCall(clazz), true);
	}

	static interface Call<C> {
		public C call(InputStream in) throws IOException;
	}
}