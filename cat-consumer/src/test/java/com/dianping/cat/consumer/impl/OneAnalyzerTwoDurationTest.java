package com.dianping.cat.consumer.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.message.internal.AbstractMessage;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.ComponentTestCase;

/**
 * The tree message is in the latest two hours
 * 
 * @author yong.you
 * 
 */
@RunWith(JUnit4.class)
public class OneAnalyzerTwoDurationTest extends ComponentTestCase {
	private static int s_count1;
	private static int s_count2;
	private static int s_period = 0;

	@Test
	public void test() throws Exception {
		MessageConsumer consumer = lookup(MessageConsumer.class, "mock");
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
		
		Thread.sleep(1000 * 2);
		Assert.assertEquals(100, s_count1);
		Assert.assertEquals(200, s_count2);
		Assert.assertEquals(2, s_period);
	}

	public static class MockAnalyzer extends
			AbstractMessageAnalyzer<AnalyzerResult> {

		public MockAnalyzer() {
			s_period++;
		}

		@Override
		protected void store(AnalyzerResult result) {
		}

		@Override
		public AnalyzerResult generate() {
			return null;
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
			// TODO Auto-generated method stub
			return false;
		}
	}

	public static class AnalyzerResult {

	}

	static class MockMessage extends AbstractMessage {
		private int type;

		public MockMessage(int type) {
			super(null, null);
			this.type = type;
		}

		@Override
		public long getTimestamp() {
			if (type == -1)
				return System.currentTimeMillis() - 60 * 60 * 1000;
			return System.currentTimeMillis();
		}

		@Override
		public void complete() {

		}
	}
}
