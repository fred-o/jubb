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
		HttpPost call = new HttpPost(path);
		HttpParams params = call.getParams();
		params.setIntParameter("priority", priority);		
		params.setParameter("data", data);
		try {
			httpClient.execute(host, call);
		} catch (ClientProtocolException cpe) {
			cpe.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
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

	/**
	 * General method for accessing the queue and getting a job object
	 * back.
	 */
	private <C> C _invoke(String op, JubbCall<C> callback) {
		try {
			HttpPost call = new HttpPost(path);
			call.getParams().setParameter("op", op);
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

	private static JubbCall<InputStream> STREAM = new JubbCall<InputStream>() {
		public InputStream call(InputStream in) {
			return in;
		}
	};

	private <T> JubbCall<T> createCall(final Class<T> clazz) {
		return new JubbCall<T>() {
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
		return _invoke("poll", createCall(clazz));
	}

	public <T> T take(final Class<T> clazz) {
		return _invoke("take", createCall(clazz));
	}

	static interface JubbCall<C> {
		public C call(InputStream in) throws IOException;
	}
}