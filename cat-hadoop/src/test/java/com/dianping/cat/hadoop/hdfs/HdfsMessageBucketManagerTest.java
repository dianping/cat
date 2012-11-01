package com.dianping.cat.hadoop.hdfs;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HdfsMessageBucketManagerTest extends ComponentTestCase {
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
		LocalMessageBucketManager localManager = (LocalMessageBucketManager) lookup(MessageBucketManager.class,
		      LocalMessageBucketManager.ID);
		MessageBucketManager hdfsManager = lookup(MessageBucketManager.class, HdfsMessageBucketManager.ID);
		MessageIdFactory factory = new MockMessageIdFactory();
		long now = 1343532130488L;
		int num = 100;

		factory.setIpAddress("7f000001");
		factory.initialize("source");
		localManager.setBaseDir(new File("target/bucket/hdfs/dump")); // make local and hdfs base dir same

		for (int i = 0; i < num; i++) {
			DefaultMessageTree tree = newMessageTree(factory.getNextId(), i, now + i * 10L);
			MessageId id = MessageId.parse(tree.getMessageId());
			localManager.storeMessage(tree,id);
		}

		localManager.close();

		for (int i = 0; i < num; i++) {
			String messageId = "source-7f000001-373203-" + i;
			MessageTree tree = hdfsManager.loadMessage(messageId);

			Assert.assertNotNull("Message " + i + " not found.", tree);
			Assert.assertEquals(messageId, tree.getMessageId());
		}

		hdfsManager.close();
	}

	static class MockMessageIdFactory extends MessageIdFactory {
		@Override
		protected long getTimestamp() {
			return 1343532130488L / 3600 / 1000;
		}
	}
}
