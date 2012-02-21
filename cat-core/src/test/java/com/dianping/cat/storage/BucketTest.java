package com.dianping.cat.storage;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.storage.MessageBucket.Direction;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class BucketTest extends ComponentTestCase {
	private DefaultMessageTree newMessageTree(String id) {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain("domain");
		tree.setHostName("hostName");
		tree.setIpAddress("ipAddress");
		tree.setMessageId(id);
		tree.setRequestToken("requestToken");
		tree.setSessionToken("sessionToken");
		tree.setThreadId("threadId");
		tree.setThreadName("threadName");

		return tree;
	}

	@Test
	public void testMessageBucket() throws Exception {
		BucketManager manager = lookup(BucketManager.class);
		MessageBucket bucket = manager.getMessageBucket("target/bucket/message");
		int groups = 10;

		// store it and load it
		for (int i = 0; i < 100; i++) {
			String id = "id" + i;
			MessageTree t1 = newMessageTree(id);
			boolean success = bucket.storeById(id, t1, "r:" + (i % groups));

			if (success) {
				MessageTree t2 = bucket.findById(id);

				Assert.assertEquals("Unable to find message after stored it.", t1.toString(), t2.toString());
			} else {
				Assert.fail("Message failed to store at i=" + i + ".");
			}
		}

		// check next message in the same thread
		for (int i = 0; i < groups - 1; i++) {
			String id = "id" + (i * groups + i);
			String nextId = "id" + ((i + 1) * groups + i);
			String tag = "r:" + i;
			MessageTree t1 = bucket.findNextById(id, Direction.FORWARD, tag);
			MessageTree t2 = bucket.findById(nextId);

			Assert.assertEquals("Unable to find next message in the thread " + i + ".", t1.toString(), t2.toString());
		}

		// close and reload it, check if everything is okay
		bucket.close();
		bucket.initialize(MessageTree.class, "target/bucket/message");

		// check next message in the same thread
		for (int i = 0; i < groups - 1; i++) {
			String id = "id" + (i * groups + i);
			String nextId = "id" + ((i + 1) * groups + i);
			String tag = "r:" + i;
			MessageTree t1 = bucket.findNextById(id, Direction.FORWARD, tag);
			MessageTree t2 = bucket.findById(nextId);

			Assert.assertEquals("Unable to find next message in the thread " + i + ".", t1.toString(), t2.toString());
		}
	}
}
