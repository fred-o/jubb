package jubb.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonJubbClient extends AbstractJubbClient {
	private static final String CLASS_HEADER = "x-Jubb-Javaclass";

	private static Call<Void> VOID = new Call<Void>() {
		public Void call(InputStream in, HttpResponse res) {
			return null;
		}
	};

	private Call<Object> genericCall = new  Call<Object>() {
		public Object call(InputStream in, HttpResponse res) throws IOException {
			try {
				Header[] h = res.getHeaders(CLASS_HEADER);
				if (h != null && h.length == 1) 
					return mapper.readValue(in, Class.forName(h[0].getValue()));
			} catch (ClassNotFoundException cnfe) {
			}
			return null;
		}
	};

	private ObjectMapper mapper = new ObjectMapper();
    
	public JsonJubbClient(URI uri, HttpClient httpClient) {
		super(uri, httpClient);
	}

	public void add(Object data) {
		try {
			StringWriter w = new StringWriter();
			mapper.writeValue(w, data);
			_invoke("add",
					Arrays.<Header>asList(new BasicHeader(CLASS_HEADER, data.getClass().getName())),
					Arrays.<NameValuePair>asList(new BasicNameValuePair("data", w.toString())),
					VOID, true);
		} catch (JsonGenerationException jge) {
			jge.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	private <T> Call<T> createJsonCall(final Class<T> clazz) {
		return new Call<T>() {
			public T call(InputStream in, HttpResponse res) throws IOException {
				return mapper.readValue(in, clazz);
			}
		};
	}

	public Object poll() {
		return _invoke("poll", null, null, genericCall, true);
	}

	public <T> T poll(Class<T> clazz) {
		return _invoke("poll", null, null, createJsonCall(clazz), true);
	}

	public Object take() {
		return _invoke("take", null, null, genericCall, true);
	}

	public <T> T take(Class<T> clazz) {
		return _invoke("take", null, null, createJsonCall(clazz), true);
	}

}