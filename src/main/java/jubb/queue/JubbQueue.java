package jubb.queue;

public interface JubbQueue {

	public void add(int priority, String data);

	public String take();

	public int size();

}