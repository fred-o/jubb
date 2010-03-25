package jubb.client;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

public class JubbProducer {
	private static final Logger LOG = Logger.getLogger(JubbProducer.class);
	private static final int MAX_RETRIES = 10;
	private static final long FIRST_RETRY_DELAY = 1;

	private JsonJubbClient client;
	private ScheduledExecutorService executorService;

	public JubbProducer() {
	}

	public void add(Object obj) {
		if (executorService == null) {
			executorService = Executors.newSingleThreadScheduledExecutor();
		} 
		executorService.schedule(new Worker(obj), 0, TimeUnit.SECONDS);
	}

	public void setClient(JsonJubbClient client) {
	    this.client = client;
	}

	public void setExecutorService(ScheduledExecutorService executorService) {
	    this.executorService = executorService;
	}

	public class Worker implements Runnable {
		private int retries = 0;
		private Object obj;

		public Worker(Object obj) {
			this.obj = obj;
		}
	    
		public void run() {
			try {
				client.add(obj);
			}
			catch (IOException ioe) {
				if(++retries > MAX_RETRIES) {
					LOG.error("Couldn't add " + obj + " to the queue even after " + retries + " retries; giving up.");
				} else {
					executorService.schedule(this, (2 ^ retries++) * FIRST_RETRY_DELAY, TimeUnit.SECONDS);
				}
			}
		}
	}
}