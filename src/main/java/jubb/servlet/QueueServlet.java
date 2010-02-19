package jubb.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class QueueServlet extends HttpServlet {

	public void init(ServletConfig cfg) {
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
		try {
			resp.getOutputStream().print("Hello, world!");
			resp.getOutputStream().flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
}