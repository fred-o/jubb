package jubb.client;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;

import org.apache.http.HttpHost;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;

import jubb.client.DirectJubbClient;

/**
 * This simple implementation connects to a Jubb queue and posts its
 * jobs synchronusly. There are no facilities for retrying failed posts.
 */
public class DirectJubbClient implements JubbClient {
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
}