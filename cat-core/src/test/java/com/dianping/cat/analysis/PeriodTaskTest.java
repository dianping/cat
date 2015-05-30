package com.dianping.cat.analysis;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.io.DefaultMessageQueue;
import com.dianping.cat.message.spi.MessageQueue;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.dianping.cat.report.ReportManager;

public class PeriodTaskTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		int size = 100;
		MessageQueue queue = new DefaultMessageQueue(size);
		MockAnalyzer analyzer = new MockAnalyzer();
		String domain = "cat";

		analyzer.initialize(start, 1000, 1000);

		PeriodTask task = new PeriodTask(analyzer, queue, start);

		for (int i = 0; i < 110; i++) {
			DefaultMessageTree tree = new DefaultMessageTree();

			tree.setDomain(domain);
			task.enqueue(tree);
		}
		task.run();

		Assert.assertEquals(size, analyzer.m_count);
		Assert.assertEquals(analyzer, task.getAnalyzer());
		Assert.assertEquals(true, task.getName().startsWith("MockAnalyzer"));
		task.shutdown();
	}

	public static class MockAnalyzer extends AbstractMessageAnalyzer<Object> {

		public int m_count;

		public int getCount() {
			return m_count;
		}

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
			if (m_count % 10 == 0) {
				throw new RuntimeException("this is for test, Please ignore it");
			}
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
