package jubb.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jubb.queue.JubbFacade;

public class QueueServlet extends HttpServlet {
	private JubbFacade facade;
	
	public void init(ServletConfig cfg) {
		this.facade = new JubbFacade(cfg);
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		facade.processGet(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) {
		facade.processPost(request, response);
	}
    
}