package jubb.queue.jq;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import jubb.queue.JubbQueue;
import jubb.queue.JubbQueueManager;
import jubb.queue.jq.JournalingQueue;
import jubb.queue.jq.JournalingQueueManager;

public class JournalingQueueManager implements JubbQueueManager {
	private static final Logger LOG = Logger.getLogger(JournalingQueueManager.class);

	private Map<String, JubbQueue> queues = new HashMap<String, JubbQueue>();
	private File baseDir;

	public JournalingQueueManager(File baseDir) throws IOException {
		this.baseDir = baseDir;
		if (!baseDir.exists() && !baseDir.mkdirs()) 
			throw new IOException("Couldn't create directory " + baseDir.getAbsolutePath() + ": ");
	}
	
	public JubbQueue getQueue(String name) {
		return queues.get(name);
	}

	public void createQueue(String name) throws IOException {
		synchronized(queues) {
			if (!queues.containsKey(name)) {
				LOG.info("Creating queue: " + name);
				queues.put(name, new JournalingQueue(new File(baseDir, name)));
			} 
		}
	}

	public Iterator<String> getQueueNames() {
		return queues.keySet().iterator();
	}
    
}