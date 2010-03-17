package jubb.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import jubb.client.JsonJubbClient;
import jubb.client.JubbConsumer;

public class JubbConsumer {
	private static final Logger LOG = Logger.getLogger(JubbConsumer.class);
	private JsonJubbClient client;
	private ScheduledExecutorService executorService;
	private Object target;
	private Map<Class<?>, Method> consumesMethods = new HashMap<Class<?>, Method>();
	private Worker worker;

	public JubbConsumer() {
	}

	

	public void start() {
		if (executorService == null) {
			executorService = Executors.newSingleThreadScheduledExecutor();
		} 
		if (worker == null) {
			worker = new Worker();
		} 
		executorService.scheduleWithFixedDelay(worker, 1, 1, TimeUnit.MILLISECONDS);
	}
	
	public void stop() {
		executorService.shutdownNow();
	}

	public void setClient(JsonJubbClient client) {
	    this.client = client;
	}

	public void setExecutorService(ScheduledExecutorService executorService) {
	    this.executorService = executorService;
	}

	public void setTarget(Object target) {
		this.consumesMethods.clear();
		for(Method m: target.getClass().getMethods()) {
			Consumes c = m.getAnnotation(Consumes.class);
			if (c != null) {
				Class<?>[] args = m.getParameterTypes();
				if (args.length != 1) {
					throw new IllegalArgumentException("Method " + m.getName() + " must only accept one argument.");
				} 
				if (args[0].isAssignableFrom(c.value())) {
					System.out.println("Found consumer method: " + m.getName());
					m.setAccessible(true);
					this.consumesMethods.put(c.value(), m);
				} 
			} 
		}
		if (consumesMethods.isEmpty()) {
			throw new IllegalArgumentException("No consumer method found ,sorry");
		} 
	    this.target = target;
	}

	class Worker implements Runnable {
		public void run() {
			Object val = client.take();
			if(val != null) {
				try {
					Method m = consumesMethods.get(val.getClass());
					if (m != null) {
						m.invoke(target, val);
					} else {
						LOG.warn("Cannot handle item with of class " + val.getClass());
					}
				
				}
				catch (InvocationTargetException ite) {
					LOG.error("Error invoking consumer", ite);
				}
				catch (IllegalAccessException iae) {
					LOG.error("Error invoking consumer", iae);
				}
			}
		}
	}
}