package com.dianping.cat.storage.dump;

import java.io.File;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.internal.MessageIdFactory;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
public class LocalMessageBucketManagerTest extends ComponentTestCase {

	private String m_baseDir = "target/bucket/dump/20120729/11/";

	private String m_outboxDir = "target/bucket/dump/outbox/20120729/11/";

	private String m_ip = "127.0.0.1";

	@Before
	public void setup() {
		new File(m_baseDir + "source-127.0.0.1-" + m_ip).delete();
		new File(m_baseDir + "source-127.0.0.1-" + m_ip + ".idx").delete();
		File file = new File(m_outboxDir + "source-127.0.0.1-" + m_ip);

		file.exists();
		file.delete();
		new File(m_outboxDir + "source-127.0.0.1-" + m_ip + ".idx").delete();

		String tmpDir = System.getProperty("java.io.tmpdir");
		new File(tmpDir, "cat-source.mark").delete();
	}

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
		tree.setParentMessageId("Cat-0a010680-384826-3");
		tree.setRootMessageId("Cat-0a010680-384826-3");
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
	public void test() {

	}

	@Test
	public void testReadWrite() throws Exception {
		LocalMessageBucketManager manager = (LocalMessageBucketManager) lookup(MessageBucketManager.class,
		      LocalMessageBucketManager.ID);
		MessageCodec codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);
		MessageIdFactory factory = new MockMessageIdFactory();

		manager.setLocalIp(m_ip);
		long now = 1343532130488L;
		int num = 5000;

		Thread.sleep(100);

		factory.setIpAddress("7f000001");
		factory.initialize("source");

		for (int i = 0; i < num; i++) {
			DefaultMessageTree tree = newMessageTree(factory.getNextId(), i, now + i * 10L);
			MessageId id = MessageId.parse(tree.getMessageId());

			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8 * 1024); // 8K
			codec.encode(tree, buf);

			tree.setBuffer(buf);
			manager.storeMessage(tree, id);
		}

		Thread.yield();

		Thread.sleep(3000);

		manager.loadMessage("source-7f000001-373203-1");

		for (int i = 0; i < num; i++) {
			String messageId = "source-7f000001-373203-" + i;
			MessageTree tree = manager.loadMessage(messageId);

			Assert.assertNotNull("Message " + i + " not found.", tree);
			Assert.assertEquals(messageId, tree.getMessageId());
		}
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
