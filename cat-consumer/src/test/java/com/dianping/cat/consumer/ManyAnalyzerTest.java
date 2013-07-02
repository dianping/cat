package com.dianping.cat.consumer;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

@RunWith(JUnit4.class)
public class ManyAnalyzerTest extends ComponentTestCase {
	private static int s_count1;

	private static int s_count2;

	private static int s_count3;

	@Test
	public void test() throws Exception {
		MessageConsumer consumer = lookup(MessageConsumer.class, "mockManyAnalyzers");

		Thread.sleep(1000);

		for (int i = 0; i < 100; i++) {
			MessageTree tree = new DefaultMessageTree();

			tree.setMessage(new MockMessage());
			consumer.consume(tree);
		}

		Thread.sleep(1000);

		Assert.assertEquals(100, s_count1);
		Assert.assertEquals(200, s_count2);
		Assert.assertEquals(300, s_count3);
	}

	public static class MockAnalyzer1 extends AbstractMessageAnalyzer<Void> {
		@Override
		protected void process(MessageTree tree) {
			s_count1++;
		}

		@Override
		protected boolean isTimeout() {
			return false;
		}

		@Override
		public Void getReport(String domain) {
			return null;
		}
	}

	public static class MockAnalyzer2 extends AbstractMessageAnalyzer<Void> {
		@Override
		protected void process(MessageTree tree) {
			s_count2 += 2;
		}

		@Override
		protected boolean isTimeout() {
			return false;
		}

		@Override
		public Void getReport(String domain) {
			return null;
		}
	}

	public static class MockAnalyzer3 extends AbstractMessageAnalyzer<Void> {
		@Override
		protected void process(MessageTree tree) {
			s_count3 += 3;
		}

		@Override
		protected boolean isTimeout() {
			return false;
		}

		@Override
		public Void getReport(String domain) {
			return null;
		}
	}

	static class MockMessage extends AbstractMessage {
		public MockMessage() {
			super(null, null);
			setTimestamp(getTimestamp());
		}

		@Override
		public long getTimestamp() {
			return System.currentTimeMillis();
		}

		@Override
		public void complete() {
		}

		@Override
		public String toString() {
			return "MockMessage";
		}
	}
}
