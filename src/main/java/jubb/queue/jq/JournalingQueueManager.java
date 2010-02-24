package jubb.queue.jq;

import java.io.File;
import java.util.HashMap;
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

	public JournalingQueueManager(File baseDir) {
		this.baseDir = baseDir;
	}
	
	public JubbQueue getQueue(String name) {
		return queues.get(name);
	}

	public void createQueue(String name) {
		synchronized(queues) {
			if (!queues.containsKey(name)) {
				LOG.info("Creating queue: " + name);
				queues.put(name, new JournalingQueue(new File(baseDir, name)));
			} 
		}
	}
    
}