package jubb.queue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import jubb.queue.JubbFacade;
import jubb.queue.JubbQueue;
import jubb.queue.JubbQueueManager;
import jubb.queue.bdb.BDBQueueManager;

import static jubb.queue.JubbFacade.Op.*;

/**
 * This class provides a simple facade for Jubb services. 
 */
public class JubbFacade {
	public enum Op {
		TAKE;
	}

	private JubbQueueManager manager;
	private ObjectMapper mapper;

	private Pattern QUEUE = Pattern.compile("/(\\w+)/?");

	public JubbFacade(ServletConfig cfg) {
		this(null, new BDBQueueManager());
	}

	public JubbFacade(File store, JubbQueueManager manager) {
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

	private void _process(final HttpServletRequest request, final HttpServletResponse response, ProcessCallback callback) {
		try {
			Op op = null;
			try {
				op = Op.valueOf(request.getParameter("op").toUpperCase());
			} catch (IllegalArgumentException iae) { 
			} catch (NullPointerException npe) { }

			String path = request.getPathInfo() != null ? request.getPathInfo() : "";

			response.setStatus(callback.process(path, op));
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			try {
				response.flushBuffer();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	public void processGet(final HttpServletRequest request, final HttpServletResponse response) {
		_process(request, response, new ProcessCallback() {
				public int process(String path, Op op) throws IOException {
					Matcher m = QUEUE.matcher(path);
					if (m.matches()) {
						JubbQueue q = manager.getQueue(m.group(1));
						if (q != null) {
							sendObject(response, new QueueStatusBean(q.size()));
							return HttpServletResponse.SC_OK;
						} 
					} 
					return HttpServletResponse.SC_NOT_FOUND;
				}
			});
	}

	public void processPost(final HttpServletRequest request, final HttpServletResponse response) {
		_process(request, response, new ProcessCallback() {
				public int process(String path, Op op) throws IOException {
					Matcher m = QUEUE.matcher(path);
					if (m.matches()) {
						JubbQueue q = manager.getQueue(m.group(1));
						if (q != null) {
							if (op == TAKE) {
								sendString(response, q.take());
							} else {
								// default is 'add'
								String data = request.getParameter("data");
								if (data != null) {
									q.add(getPriority(request), data);
								} 
							}
							return HttpServletResponse.SC_OK;

						} 
					} 
					return HttpServletResponse.SC_NOT_FOUND;
				}
			});
	}

	static interface ProcessCallback {
		public int process(String path, Op op) throws IOException;
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