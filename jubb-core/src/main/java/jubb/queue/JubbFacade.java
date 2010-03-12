package jubb.queue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;

import jubb.queue.JubbQueue;
import jubb.queue.jq.JournalingQueueManager;

/**
 * This class provides a simple facade for Jubb services. 
 */
public class JubbFacade {
	public enum Op {
		POLL, TAKE, ADD;
	}

	private JubbQueueManager manager;
	private ObjectMapper mapper;

	private Pattern ROOT = Pattern.compile("^/?$");
	private Pattern QUEUE = Pattern.compile("^/(\\w+)/?$");

	public JubbFacade(ServletConfig cfg) throws ServletException {
		this.mapper = new ObjectMapper();
		try {
			this.manager =  new JournalingQueueManager(getBaseDir(cfg));
			manager.createQueue("test");
		} catch (IOException ioe) {
			throw new ServletException("Exception during init()", ioe);
		}
	}

	private File getBaseDir(ServletConfig cfg) {
		// TODO: do proper resolution based on servlet config
		return new File(new File(System.getProperty("user.home")), ".jubb");
	}

	protected void sendString(HttpServletResponse response, Job job) throws IOException {
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
		try {
			w.append(job.data);
		} finally {
			w.flush();
			w.close();
		}
	}

	protected void sendObject(HttpServletResponse response, Object obj) throws IOException {
		mapper.writeValue(response.getOutputStream(), obj);
	}

	private void _process(final HttpServletRequest request, final HttpServletResponse response, ProcessCallback callback) {
		try {
			Op op = null;
			try {
				op = Op.valueOf(request.getParameter("op").trim().toUpperCase());
			} catch (IllegalArgumentException iae) { 
				iae.printStackTrace();
			} catch (NullPointerException npe) { }

			String path = request.getPathInfo() != null ? request.getPathInfo() : "";

			response.setStatus(callback.process(path, op));
		} catch (InterruptedException ie) {
			// TODO: handle timeout
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
					m = ROOT.matcher(path);
					if (m.matches()) {
						Map<String, QueueStatusBean> qs = new HashMap<String, QueueStatusBean>();
						for(Iterator<String> iter = manager.getQueueNames(); iter.hasNext(); ) {
							String name = iter.next();
							JubbQueue q = manager.getQueue(name);
						    QueueStatusBean sb = new QueueStatusBean(q.size());
							qs.put(name, sb);
							sendObject(response, qs);
						}
					} 
					return HttpServletResponse.SC_NOT_FOUND;
				}
			});
	}

	private List<String[]> extractMetadata(HttpServletRequest request) {
		List<String[]> meta = new LinkedList<String[]>();
		for(Iterator iter = request.getParameterMap().entrySet().iterator(); iter.hasNext();) {
			Map.Entry<String, String> entry = (Map.Entry<String, String>) iter.next();
			if (entry.getKey().startsWith("x-Jubb-"))
				meta.add(new String[] { entry.getKey(), entry.getValue() });
		}
		return meta;
	}

	public void processPost(final HttpServletRequest request, final HttpServletResponse response) {
		_process(request, response, new ProcessCallback() {
				public int process(String path, Op op) throws IOException, InterruptedException {
					Matcher m = QUEUE.matcher(path);
					if (m.matches()) {
						JubbQueue q = manager.getQueue(m.group(1));
						if (q != null) {
							if (op == Op.POLL) {
								sendString(response, q.poll());
							} else if (op == Op.TAKE) {
								sendString(response, q.take());
							} else {
								// default is 'add'
								String data = request.getParameter("data");
								if (data != null) {
									q.add(new Job(System.currentTimeMillis(), data, null));
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
		public int process(String path, Op op) throws IOException, InterruptedException;
	}

	public class QueueStatusBean {
		private int size;
		
		public QueueStatusBean(int size) {
			this.size = size;
		}

		public int getSize() {
			return size;
		}
	}
 
}