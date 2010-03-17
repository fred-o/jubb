package jubb.client;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import jubb.client.JsonJubbClient;
import jubb.client.JubbClient;
import jubb.client.JubbConsumer;

public abstract class JubbConsumer {
	private static final Logger LOG = Logger.getLogger(JubbConsumer.class);
	private JsonJubbClient client;
	private ScheduledExecutorService executorService;
	private Method consumesMethod;
	private Class<?> consumesClass;
	private Worker worker;

	public JubbConsumer() {
		for(Method m: getClass().getMethods()) {
			Consumes c = m.getAnnotation(Consumes.class);
			if (c != null) {
				if (this.consumesMethod != null) {
					throw new IllegalArgumentException("Only one consumer method allowed, sorry.");
				} 
				Class<?>[] args = m.getParameterTypes();
				if (args.length != 1) {
					throw new IllegalArgumentException("Method " + m.getName() + " can only accept ono argument.");
				} 
				if (args[0].isAssignableFrom(c.value())) {
					System.out.println("Found consumer method: " + m.getName());
					this.consumesClass = c.value();
					this.consumesMethod = m;
					m.setAccessible(true);
				} 
			} 
		}
		if (consumesMethod == null) {
			throw new IllegalArgumentException("No consumer method found ,sorry");
		} 
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

	class Worker implements Runnable {

		public void run() {
			Object val = client.take();
			try {
				consumesMethod.invoke(JubbConsumer.this, val);
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