package jubb.queue.jq;

import java.io.File;
import java.io.IOException;

import jubb.queue.Job;
import jubb.queue.jq.JournalingQueue;

import junit.framework.TestCase;

@SuppressWarnings({"unchecked", "unused"})
public class JournalingQueueTest extends TestCase {

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
    
	public void testAddRemove() throws Exception {
		JournalingQueue jq1 = new JournalingQueue(baseDir);
		jq1.add(new Job(0, "test1", null));
		jq1.add(new Job(0, "test2", null));
		jq1.add(new Job(0, "test3", null));
		assertEquals(3, jq1.size());
		assertEquals("test1", jq1.poll().data);
		assertEquals(2, jq1.size());
		jq1.add(new Job(0, "test1", null));
		assertEquals(3, jq1.size());
		jq1.close();
		jq1 = null;

		JournalingQueue jq2 = new JournalingQueue(baseDir);
		assertEquals(3, jq2.size());
		assertEquals("test2", jq2.poll().data);
		assertEquals("test3", jq2.poll().data);
		assertEquals("test1", jq2.poll().data);
		jq2.close();
	}
}