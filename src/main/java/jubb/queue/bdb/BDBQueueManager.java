package jubb.queue.bdb;

import jubb.queue.JubbQueue;
import jubb.queue.JubbQueueManager;
import jubb.queue.bdb.BDBQueue;

public class BDBQueueManager implements JubbQueueManager {

	BDBQueue q = new BDBQueue(null);
    
	public JubbQueue getQueue(String name) {
		if ("test".equals(name)) {
			return q;
		} 
		return null;
	}

	public void createQueue(String name) {

	}


}