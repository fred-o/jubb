package jubb.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

public class QueueServlet extends HttpServlet {

	
	public void init(ServletConfig cfg) {

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {

			JsonFactory jf = new JsonFactory();

			JsonGenerator g = jf.createJsonGenerator(resp.getOutputStream(), JsonEncoding.UTF8);
			g.writeStartObject();
			g.writeObjectFieldStart("jubb");
			g.writeStringField("message", "Hello, world!");
			g.writeEndObject();
			g.writeEndObject();
			g.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
    
}