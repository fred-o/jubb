package jubb.queue.jq;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import jubb.queue.JubbQueue;
import jubb.queue.jq.JournalingQueue;

public class JournalingQueue implements JubbQueue {
	private PriorityBlockingQueue<Job> _queue;
	private Journal journal;

	public JournalingQueue(File dir) throws IOException {
		this.journal = new Journal(dir);
		this._queue = journal.restore();

		if (!dir.exists()) dir.mkdirs();
	}

	public void add(int priority, String data) {
		Job job = new Job(priority, System.currentTimeMillis(), data);
		this.journal.appendAdd(job);
		this._queue.add(job);

		System.out.println("ADD. " + _queue);
	}

	public String take() throws InterruptedException {
		Job job = this._queue.take();
		this.journal.appendRemove(job);
		return job.data;
	}

	public String poll() {
		Job job = this._queue.poll();
		if (job != null) {
			this.journal.appendRemove(job);
			return job.data;
		} 
		return null;
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

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Job))
				return false;
			return this.compareTo((Job)obj) == 0;
		}

		@Override
		public String toString() {
			return "[time=" + timestamp + ",prio=" + priority + ",data="+ data + "]";
		}
	}
	
}