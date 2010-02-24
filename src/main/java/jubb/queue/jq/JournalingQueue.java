package jubb.queue.jq;

import java.io.File;
import java.util.concurrent.PriorityBlockingQueue;

import jubb.queue.JubbQueue;
import jubb.queue.jq.JournalingQueue;

public class JournalingQueue implements JubbQueue {
	private PriorityBlockingQueue<Job> _queue;

	public JournalingQueue(File dir) {
		this._queue = new PriorityBlockingQueue<Job>();
	}

	public void add(int priority, String data) {
		System.out.println("ADD: " + priority + ", " + data);

		Job job = new Job(priority, System.currentTimeMillis(), data);
		this._queue.add(job);

		System.out.println(_queue);
	}

	public String take() throws InterruptedException {
		Job job = this._queue.take();
		return job.data;
	}

	public String poll() {
		Job job = this._queue.poll();
		return job != null ? job.data : null;
	}

	public int size() {
		return _queue.size();
	}
	
	static class Job implements Comparable<Job> {
		final long timestamp;
		final int priority;
		final String data;

		public Job(int priority, long timestamp, String data) {
			this.priority = priority;
			this.timestamp = timestamp;
			this.data = data;
		}

		public int compareTo(Job other) {
			int ret = this.priority - other.priority;
			if (ret != 0) 
				return ret;
			return (int) (this.timestamp - other.timestamp);
		}
		
	}
	
}