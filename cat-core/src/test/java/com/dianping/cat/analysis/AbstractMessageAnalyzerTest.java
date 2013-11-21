package com.dianping.cat.analysis;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class AbstractMessageAnalyzerTest {

	@Test
	public void test() throws InterruptedException {
		MessageQueue queue = new DefaultMessageQueue(1000);
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);

		MockAnalyzer analyzer = new MockAnalyzer();
		analyzer.initialize(start, 1000, 1000);

		Assert.assertEquals(true, analyzer.isActive());

		int count = 1000;
		for (int i = 0; i < count; i++) {
			queue.offer(new DefaultMessageTree());
		}

		analyzer.analyze(queue);

		Assert.assertEquals(count, analyzer.m_count);
		Assert.assertEquals(true, analyzer.isActive());
		
		Thread.sleep(2000);
		Assert.assertEquals(true, analyzer.isTimeout());
	}

	public static class MockAnalyzer extends AbstractMessageAnalyzer<Object> {

		public int m_count;

		@Override
		public void doCheckpoint(boolean atEnd) {
		}

		@Override
		public Object getReport(String domain) {
			return null;
		}

		@Override
		protected void process(MessageTree tree) {
			m_count++;
		}
	}
}
