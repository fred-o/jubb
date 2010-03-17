package jubb.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class AbstractJubbClient {
	private static final Logger LOG = Logger.getLogger(AbstractJubbClient.class);
	private HttpClient httpClient;
	private URI uri;

	public AbstractJubbClient(URI uri, HttpClient httpClient) {
		this.uri = uri;
		this.httpClient = httpClient;
	}

	/**
	 * General method for accessing the queue and getting a job object
	 * back.
	 */
	protected <C> C _invoke(String op, List<Header> metadata, List<NameValuePair> content,
			Call<C> callback, boolean close) {
		try {
			HttpPost call = new HttpPost(uri);
			if (metadata != null) {
				for(Header meta: metadata) 
				    call.addHeader(meta);
			} 
			List<NameValuePair> vals = new LinkedList<NameValuePair>();
			vals.add(new BasicNameValuePair("op", op));
			if (content != null) 
				vals.addAll(content);
			call.setEntity(new UrlEncodedFormEntity(vals));

			call.getParams().setIntParameter("http.connection.timeout", 10000);

			HttpResponse res = httpClient.execute(call);
			if (res.getStatusLine().getStatusCode() == 200) {
				InputStream in = res.getEntity().getContent();
				try {
					return callback.call(in, res);
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

	static interface Call<C> {

		public C call(InputStream in, HttpResponse res) throws IOException;
	}
}