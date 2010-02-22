package jubb.queue;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import jubb.client.Job;
import jubb.queue.JubbFacade.QueueStatusBean;
import jubb.queue.JubbQueue;
import jubb.queue.JubbQueueManager;
import jubb.queue.bdb.BDBQueueManager;

/**
 * This class provides a simple facade for Jubb services. 
 */
public class JubbFacade {
	private JubbQueueManager manager;
	private ObjectMapper mapper;

	private Pattern QUEUE = Pattern.compile("/(\\w+)/?");

	public JubbFacade(String mountPoint, File store) {
		this(mountPoint, store, new BDBQueueManager());
	}

	public JubbFacade(String mountPoint, File store, JubbQueueManager manager) {

		this.manager = manager;
		this.mapper = new ObjectMapper();

	}

	protected void sendString(HttpServletResponse response, String data) throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
		try {
			w.append(data);
		} finally {
			w.flush();
			w.close();
		}
	}

	protected void sendObject(HttpServletResponse response, Object obj) throws IOException {
		mapper.writeValue(response.getOutputStream(), obj);
	}

	protected int getPriority(HttpServletRequest request) {
		try {
			return Integer.parseInt(request.getParameter("priority"));
		} catch (NumberFormatException nfe) {
			return 0;
		}
	}

	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			String path = request.getPathInfo();
			if (path != null) {
				Matcher m = QUEUE.matcher(path);
				if (m.matches()) {
					JubbQueue q = manager.getQueue(m.group(1));
					if (q != null) {
						if ("POST".equals(request.getMethod())) {
							String op = request.getParameter("op");
							if ("take".equals(op)) {
								sendString(response, q.take());
							} else {
								// default is 'add'
								String data = request.getParameter("data");
								if (data != null) {
									q.add(getPriority(request), data);
								} 
							}
							response.sendError(HttpServletResponse.SC_OK);
							return;
						} else if ("GET".equals(request.getMethod())) {
							// send status
							sendObject(response, new QueueStatusBean(q.size()));
							return;
						} 
						response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED);
						return;
					} 
				} 
			} 
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} finally {
			response.flushBuffer();
		}
	}

	public class QueueStatusBean {
		private int size;
		
		public QueueStatusBean(int size) {
			this.size = size;
		}

		public int getSize() {
		    return size;
		}
		
		public void setSize(int size) {
		    this.size = size;
		}
	}
 
}