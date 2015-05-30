package com.dianping.cat.analysis;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Threads;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.ReportManager;

public class AbstractMessageAnalyzerTest extends ComponentTestCase {

	@Test
	public void testTimeOut() throws InterruptedException {
		int queueSize = 1000;
		MessageQueue queue = new DefaultMessageQueue(queueSize);
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);

		MockAnalyzer analyzer = new MockAnalyzer();
		analyzer.initialize(start, 1000, 1000);

		Assert.assertEquals(true, analyzer.isActive());
		Assert.assertEquals(true, analyzer.isTimeout());

		int count = 2000;
		for (int i = 0; i < count; i++) {
			queue.offer(new DefaultMessageTree());
		}

		analyzer.analyze(queue);

		Assert.assertEquals(Math.min(queueSize, count), analyzer.m_count);
		Assert.assertEquals(true, analyzer.isActive());
		Assert.assertEquals(true, analyzer.isTimeout());

		Thread.sleep(2000);
		Assert.assertEquals(true, analyzer.isTimeout());
		Assert.assertEquals(1000, analyzer.getExtraTime());
		Assert.assertEquals(start, analyzer.getStartTime());
	}

	@Test
	public void testNotTimeOut() throws InterruptedException {
		MessageQueue queue = new DefaultMessageQueue(1000);
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);

		MockAnalyzer analyzer = new MockAnalyzer();
		analyzer.initialize(start, 60 * 60 * 1000, 1000);

		Assert.assertEquals(true, analyzer.isActive());
		Assert.assertEquals(false, analyzer.isTimeout());

		int count = 1000;
		for (int i = 0; i < count; i++) {
			queue.offer(new DefaultMessageTree());
		}
		Threads.forGroup().start(new ShutDown(analyzer));

		analyzer.analyze(queue);

		Assert.assertEquals(count, analyzer.m_count);
		Assert.assertEquals(false, analyzer.isTimeout());

		Thread.sleep(2000);
		Assert.assertEquals(false, analyzer.isTimeout());
	}

	public static class ShutDown implements Runnable {
		private MockAnalyzer m_analyzer;

		public ShutDown(MockAnalyzer analyzer) {
			m_analyzer = analyzer;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
			m_analyzer.shutdown();
			Assert.assertEquals(true, m_analyzer.isActive());
		}

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
			throw new RuntimeException("this is for test, Please ignore it");
		}

		@Override
      protected void loadReports() {
      }

		@Override
      public ReportManager<?> getReportManager() {
	      // TODO Auto-generated method stub
	      return null;
      }
	}
	
}
