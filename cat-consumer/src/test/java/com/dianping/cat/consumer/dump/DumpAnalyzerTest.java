package com.dianping.cat.consumer.dump;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.MockLog;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.DefaultTransaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

public class DumpAnalyzerTest {

	@Test
	public void test() throws Exception {
		DumpAnalyzer analyzer = new DumpAnalyzer();
		int size = 1000;
		MockLocalMessageBucketManager bucketManager = new MockLocalMessageBucketManager();

		analyzer.setServerStateManager(new ServerStatisticManager());
		analyzer.setBucketManager(bucketManager);
		analyzer.enableLogging(new MockLog());

		for (int i = 0; i < size; i++) {
			analyzer.process(generateMessageTree(i));
		}

		Assert.assertEquals(size, bucketManager.m_insert);

		analyzer.doCheckpoint(true);
		Thread.sleep(1000);

		Assert.assertEquals(true, bucketManager.m_archive);

		for (int i = 0; i < size; i++) {
			analyzer.process(generateOldMessageTree());
		}

		Assert.assertEquals(size, bucketManager.m_insert);

		for (int i = 0; i < size; i++) {
			analyzer.process(generateErrorMessageTree());
		}

		Assert.assertEquals(size, bucketManager.m_insert);
	}

	protected MessageTree generateOldMessageTree() {
		DefaultMessageTree tree = (DefaultMessageTree) generateMessageTree(10);

		tree.setMessageId("Cat-0a010680-1385467200000-10");

		return tree;
	}

	protected MessageTree generateErrorMessageTree() {
		DefaultMessageTree tree = (DefaultMessageTree) generateMessageTree(10);

		tree.setMessageId("Cat-0a010680-184852-10");

		return tree;
	}

	protected MessageTree generateMessageTree(int i) {
		MessageTree tree = new DefaultMessageTree();

		tree.setDomain("cat");
		tree.setHostName("group001");
		tree.setIpAddress("192.168.1.1");

		DefaultTransaction t = new DefaultTransaction("A", "n" + i % 2, null);
		DefaultTransaction t2 = new DefaultTransaction("A-1", "n" + i % 3, null);

		if (i % 2 == 0) {
			t2.setStatus("ERROR");
		} else {
			t2.setStatus(Message.SUCCESS);
		}

		t2.complete();
		t2.setDurationInMillis(i);

		t.addChild(t2);

		if (i % 2 == 0) {
			t.setStatus("ERROR");
		} else {
			t.setStatus(Message.SUCCESS);
		}

		t.complete();
		t.setDurationInMillis(i * 2);
		long timestamp = 1385467200000L;

		t.setTimestamp(timestamp + 1000);
		t2.setTimestamp(timestamp + 2000);
		tree.setMessage(t);
		tree.setMessageId("Cat-0a010680-384852-" + i);

		return tree;
	}

	public static class MockLocalMessageBucketManager extends LocalMessageBucketManager {

		protected boolean m_archive = false;

		protected int m_insert = 0;

		@Override
		public MessageTree loadMessage(String messageId) {
			return new DefaultMessageTree();
		}

		@Override
		public void storeMessage(MessageTree tree, MessageId id) {
			m_insert++;
		}

		@Override
		public void archive(long startTime) {
			m_archive = true;
		}
	}
}
