package jubb.queue;

import java.io.Serializable;
import java.util.List;

public class Job implements Serializable {
	private static final long serialVersionUID = -991879344276791444L;
	public final long timestamp;
	public final String data;
	public final List<String[]> metadata;

	public Job(long timestamp, String data, List<String[]> metadata) {
		if (data == null) 
			throw new IllegalArgumentException("data cannot be null");
		this.timestamp = timestamp;
		this.data = data;
		this.metadata = metadata;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Job))
			return false;
		Job other = (Job) obj;
		if (this.timestamp != other.timestamp) 
			return false;
		return this.data.equals(other.data);
	}

	@Override
	public String toString() {
		return "[time=" + timestamp + ",data="+ data + ",meta=" + metadata + "]";
	}
}