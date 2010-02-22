package jubb.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;

import jubb.queue.JubbFacade;

public class QueueServlet extends HttpServlet {
	private JubbFacade facade;
	
	public void init(ServletConfig cfg) {
		this.facade = new JubbFacade("/queue", null);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {
			facade.processRequest(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
		try {
			facade.processRequest(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
}