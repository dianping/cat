package com.dianping.cat.consumer.dump;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

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
import com.dianping.cat.message.storage.MessageBucketManager;

@RunWith(JUnit4.class)
public class LocalMessageBucketManagerTest extends ComponentTestCase {

	private String m_baseDir = "target/bucket/dump/20120729/11/";

	private String m_outboxDir = "target/bucket/dump/outbox/20120729/11/";

	private String m_ip = "127.0.0.1";

	private MessageCodec m_codec;

	private LocalMessageBucketManager m_manager;

	private long m_now = 1343532130488L;

	private int m_threadNum = 20;

	private int m_num = 1000;

	public void clear(String domain, String ip) {
		new File(m_baseDir + domain + "-" + ip + "-" + ip).delete();
		new File(m_baseDir + domain + "-" + ip + "-" + ip + ".idx").delete();

		new File(m_outboxDir + domain + "-" + ip + "-" + ip).delete();
		new File(m_outboxDir + domain + "-" + ip + "-" + ip + ".idx").delete();

		new File(System.getProperty("java.io.tmpdir"), "cat-" + domain + ".mark").delete();
	}

	private MessageIdFactory getMessageIdFactory(String ip, String domain) throws IOException {
		MessageIdFactory factory = new MockMessageIdFactory();

		factory.setIpAddress(ip);
		factory.initialize(domain);

		return factory;
	}

	private DefaultMessageTree newMessageTree(String id, int i, long timestamp) {
		DefaultMessageTree tree = new DefaultMessageTree();

		tree.setMessageId(id);
		tree.setDomain("target");
		tree.setHostName("localhost");
		tree.setIpAddress("127.0.0.1");
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

	@Before
	public void setup() throws Exception {
		m_codec = lookup(MessageCodec.class, PlainTextMessageCodec.ID);

		m_manager = (LocalMessageBucketManager) lookup(MessageBucketManager.class, LocalMessageBucketManager.ID);
		m_manager.setLocalIp(m_ip);

		clear("source", m_ip);
		
		for (int i = 0; i < m_threadNum; i++) {
			clear("source" + i, m_ip);
		}
	}

	@Test
	public void testMultiThreadRW() {
		try {
			CountDownLatch latch = new CountDownLatch(m_threadNum);

			for (int i = 0; i < m_threadNum; i++) {
				ReadAndWriteBucketManagerThread thread = new ReadAndWriteBucketManagerThread("7f000001", "source" + i,
				      latch);
				thread.setName("LocalMessageBucket-" + i);

				thread.start();
			}

			latch.await();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void testReadWrite(String ip, String domain) throws Exception {
		MessageIdFactory factory = getMessageIdFactory(ip, domain);

		for (int i = 0; i < m_num; i++) {
			String messageId = factory.getNextId();
			DefaultMessageTree tree = newMessageTree(messageId, i, m_now + i * 10L);
			MessageId id = MessageId.parse(tree.getMessageId());
			ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(512);
			
			m_codec.encode(tree, buf);

			tree.setBuffer(buf);
			m_manager.storeMessage(tree, id);
		}
		
		Thread.sleep(1000);

		for (int i = 0; i < m_num; i++) {
			String messageId = domain + "-" + ip + "-373203-" + i;
			MessageTree tree = m_manager.loadMessage(messageId);

			Assert.assertNotNull("Message " + i + " not found.", tree);
			if (tree != null) {
				Assert.assertEquals(messageId, tree.getMessageId());
			}
		}
	}

	@Test
	public void testSingleThreadRW() throws Exception {
		testReadWrite("7f000001", "source");
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

	class ReadAndWriteBucketManagerThread extends Thread {
		private String m_ip;

		private String m_domain;

		private CountDownLatch m_latch;

		public ReadAndWriteBucketManagerThread(String ip, String domain, CountDownLatch latch) {
			m_ip = ip;
			m_domain = domain;
			m_latch = latch;
		}

		@Override
		public void run() {
			try {
				testReadWrite(m_ip, m_domain);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				m_latch.countDown();
			}
		}

	}
}
