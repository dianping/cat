package com.dianping.cat.consumer.demo;

import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

/**
 * The tree message is in the latest two hours
 */
@RunWith(JUnit4.class)
public class OneAnalyzerTwoDurationTest extends ComponentTestCase {
	private static int s_count1;

	private static int s_count2;

	private static int s_period = 0;

	@Before
	public void before() {
	}

	@Test
	public void test() throws Exception {
		MessageConsumer consumer = lookup(MessageConsumer.class, "mock");

		Thread.sleep(1000);
		for (int i = 0; i < 100; i++) {
			DefaultMessageTree tree = new DefaultMessageTree();
			tree.setMessage(new MockMessage(-1));
			consumer.consume(tree);
		}

		for (int i = 0; i < 100; i++) {
			DefaultMessageTree tree = new DefaultMessageTree();
			tree.setMessage(new MockMessage(1));
			consumer.consume(tree);
		}

		Thread.sleep(1000);
		Assert.assertEquals(0, s_count1);
		Assert.assertEquals(400, s_count2);
		Assert.assertEquals(1, s_period);
	}

	public static class MockAnalyzer extends AbstractMessageAnalyzer<Void> {
		public MockAnalyzer() {
			s_period++;
		}

		@Override
		protected void process(MessageTree tree) {
			long time = tree.getMessage().getTimestamp();
			long systemTime = System.currentTimeMillis();
			if (systemTime - time > 30 * 60 * 1000)
				s_count1++;
			else
				s_count2 = s_count2 + 2;
		}

		@Override
		protected boolean isTimeout() {
			return false;
		}

		@Override
		public Void getReport(String domain) {
			return null;
		}

		@Override
		public Set<String> getDomains() {
			return null;
		}
	}

	static class MockMessage extends AbstractMessage {
		private int m_type;

		public MockMessage(int type) {
			super(null, null);
			m_type = type;
			setTimestamp(getTimestamp());
		}

		public int getIntType() {
			return m_type;
		}

		@Override
		public long getTimestamp() {
			return System.currentTimeMillis();
		}

		@Override
		public void complete() {
		}
	}
}
