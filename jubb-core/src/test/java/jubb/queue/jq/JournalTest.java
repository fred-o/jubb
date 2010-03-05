package jubb.queue.jq;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import jubb.queue.jq.JournalInput;
import jubb.queue.jq.JournalOutput;
import jubb.queue.jq.JournalingQueue;

import junit.framework.TestCase;

@SuppressWarnings({"unchecked"})
public class JournalTest extends TestCase {

	private File baseDir;
    
	private void cleanDir(File d) throws IOException {
		for(File f: d.listFiles()) {
		    f.delete();
		}
	}
   
	public void setUp() throws Exception {
	    baseDir = new File("src/test/tmp");
		baseDir.mkdirs();
		cleanDir(baseDir);
	}

	public void testSingleAddAndRestore() throws Exception {
		JournalOutput jo = new JournalOutput(baseDir, new LinkedBlockingQueue<JournalingQueue.Job>());
		JournalingQueue.Job j1 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test\"}");
		jo.appendAdd(j1);
		jo.close();

		JournalInput ji = new JournalInput(baseDir);
		BlockingQueue q = ji.restore();
		assertNotNull(q);
		assertEquals(1, q.size());
		assertEquals(j1, q.take());
	}

	public void testAddAndRestoreSeveralJobs() throws Exception {
		JournalOutput jo = new JournalOutput(baseDir, new LinkedBlockingQueue<JournalingQueue.Job>());
		JournalingQueue.Job j1 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test1\"}");
		JournalingQueue.Job j2 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test2\"}");
		JournalingQueue.Job j3 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test3\"}");
		jo.appendAdd(j1);
		jo.appendAdd(j2);
		jo.appendAdd(j3);
		jo.close();

		JournalInput ji = new JournalInput(baseDir);
		BlockingQueue q = ji.restore();
		assertNotNull(q);
		assertEquals(3, q.size());
		assertEquals(j1, q.take());
		assertEquals(j2, q.take());
		assertEquals(j3, q.take());
	}

	public void testAddRemoveAndRestoreSeveralJobs() throws Exception {
		JournalOutput jo = new JournalOutput(baseDir, new LinkedBlockingQueue<JournalingQueue.Job>());
		JournalingQueue.Job j1 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test1\"}");
		JournalingQueue.Job j2 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test2\"}");
		JournalingQueue.Job j3 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test3\"}");
		jo.appendAdd(j1);
		jo.appendAdd(j2);
		jo.appendAdd(j3);
		jo.appendRemove(j2);
		jo.appendRemove(j1);
		jo.appendAdd(j1);

		jo.close();

		JournalInput ji = new JournalInput(baseDir);
		BlockingQueue q = ji.restore();
		assertNotNull(q);
		assertEquals(2, q.size());
		assertEquals(j3, q.take());
		assertEquals(j1, q.take());
	}

	public void testCreateAndRestoreSnapshot() throws Exception {
	    JournalOutput jo = new JournalOutput(baseDir, new LinkedBlockingQueue<JournalingQueue.Job>());
		JournalingQueue.Job j1 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test1\"}");
		JournalingQueue.Job j2 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test2\"}");
		JournalingQueue.Job j3 = new JournalingQueue.Job(System.currentTimeMillis(), "{\"message\":\"test3\"}");
		BlockingQueue<JournalingQueue.Job> q1 = new LinkedBlockingQueue<JournalingQueue.Job>();
		q1.add(j1);
		q1.add(j2);
		q1.add(j3);

		jo.snapshot(q1);
		jo.close();
		
		JournalInput ji = new JournalInput(baseDir);
		BlockingQueue<JournalingQueue.Job> q2 = ji.restore();
		assertNotNull(q2);
		assertEquals(3, q2.size());
		assertEquals(j1, q2.poll());
		assertEquals(j2, q2.poll());
		assertEquals(j3, q2.poll());
	}

}