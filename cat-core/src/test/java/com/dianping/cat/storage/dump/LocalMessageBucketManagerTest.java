package com.dianping.cat.storage.dump;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class LocalMessageBucketManagerTest extends ComponentTestCase {
	private DefaultMessageTree newMessageTree(String id, int i, long timestamp) {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setDomain("target");
		tree.setHostName("localhost");
		tree.setIpAddress("127.0.0.1");
		tree.setMessageId(id);
		tree.setParentMessageId("parentMessageId" + i);
		tree.setRootMessageId("rootMessageId" + i);
		tree.setSessionToken("sessionToken");
		tree.setThreadGroupName("threadGroupName");
		tree.setThreadId("threadId" + i);
		tree.setThreadName("threadName");

		tree.setMessage(newTransaction("type", "name" + i, timestamp, "0", 123456 + i, "data" + i));
		return tree;
	}

	private Transaction newTransaction(String type, String name, long timestamp, String status, int duration, String data) {
		DefaultTransaction transaction = new DefaultTransaction(type, name, null);

		transaction.setStatus(status);
		transaction.addData(data);
		transaction.complete();
		transaction.setTimestamp(timestamp);
		transaction.setDurationInMillis(duration);
		return transaction;
	}

	@Test
	public void testReadWrite() throws Exception {
		MessageBucketManager manager = lookup(MessageBucketManager.class, LocalMessageBucketManager.ID);
		MessageIdFactory factory = new MockMessageIdFactory();
		long now = 1343532130488L;
		int num = 100;

		Thread.sleep(100);

		factory.setIpAddress("7f000001");
		factory.initialize("source");

		for (int i = 0; i < num; i++) {
			manager.storeMessage(newMessageTree(factory.getNextId(), i, now + i * 10L));
		}

		Thread.yield();

		for (int i = 0; i < num; i++) {
			String messageId = "source-7f000001-373203-" + i;
			MessageTree tree = manager.loadMessage(messageId);

			Assert.assertNotNull("Message " + i + " not found.", tree);
			Assert.assertEquals(messageId, tree.getMessageId());
		}

		manager.close();
	}

	static class MockMessageIdFactory extends MessageIdFactory {
		@Override
		protected long getTimestamp() {
			return 1343532130488L / 3600 / 1000;
		}

		@Override
		public void initialize(String domain) throws IOException {
			super.initialize(domain);
			super.resetIndex();
		}
	}
}
