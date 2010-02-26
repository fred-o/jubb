package jubb.queue.jq;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.PriorityBlockingQueue;

import jubb.queue.jq.Journal;
import jubb.queue.jq.JournalingQueue;

import junit.framework.TestCase;

@SuppressWarnings({"unchecked", "unused"})
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
		Journal jn1 = new Journal(baseDir);
		JournalingQueue.Job j1 = new JournalingQueue.Job(10, System.currentTimeMillis(), "{\"message\":\"test\"}");
		jn1.appendAdd(j1);
		jn1.close();

		Journal jn2 = new Journal(baseDir);
		PriorityBlockingQueue q = jn2.restore();
		assertNotNull(q);
		assertEquals(1, q.size());
		assertEquals(j1, q.take());
	}

	public void testAddAndRestoreSeveralJobs() throws Exception {
		Journal jn1 = new Journal(baseDir);
		JournalingQueue.Job j1 = new JournalingQueue.Job(10, System.currentTimeMillis(), "{\"message\":\"test1\"}");
		JournalingQueue.Job j2 = new JournalingQueue.Job(10, System.currentTimeMillis(), "{\"message\":\"test2\"}");
		JournalingQueue.Job j3 = new JournalingQueue.Job(10, System.currentTimeMillis(), "{\"message\":\"test3\"}");
		jn1.appendAdd(j1);
		jn1.appendAdd(j2);
		jn1.appendAdd(j3);
		jn1.close();

		Journal jn2 = new Journal(baseDir);
		PriorityBlockingQueue q = jn2.restore();
		assertNotNull(q);
		assertEquals(3, q.size());
		assertEquals(j1, q.take());
		assertEquals(j2, q.take());
		assertEquals(j3, q.take());
	}

	public void testAddRemoveAndRestoreSeveralJobs() throws Exception {
		Journal jn1 = new Journal(baseDir);
		JournalingQueue.Job j1 = new JournalingQueue.Job(10, System.currentTimeMillis(), "{\"message\":\"test1\"}");
		JournalingQueue.Job j2 = new JournalingQueue.Job(0, System.currentTimeMillis(), "{\"message\":\"test2\"}");
		JournalingQueue.Job j3 = new JournalingQueue.Job(10, System.currentTimeMillis(), "{\"message\":\"test3\"}");
		jn1.appendAdd(j1);
		jn1.appendAdd(j2);
		jn1.appendAdd(j3);
		jn1.appendRemove(j2);
		jn1.appendRemove(j1);
		jn1.appendAdd(j1);

		jn1.close();

		Journal jn2 = new Journal(baseDir);
		PriorityBlockingQueue q = jn2.restore();
		assertNotNull(q);
		assertEquals(2, q.size());
		assertEquals(j3, q.take());
		assertEquals(j1, q.take());
	}

}