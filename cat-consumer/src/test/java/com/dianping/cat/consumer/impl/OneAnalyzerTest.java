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

@RunWith(JUnit4.class)
public class OneAnalyzerTest extends ComponentTestCase {
	private static int s_count;

	@Test
	public void test() throws Exception {
		MessageConsumer consumer = lookup(MessageConsumer.class, "mock");

		for (int i = 0; i < 100; i++) {
			DefaultMessageTree tree = new DefaultMessageTree();
			tree.setMessage(new MockMessage());
			consumer.consume(tree);
		}
		
		Thread.sleep(1000*2);
		Assert.assertEquals(100, s_count);
	}

	public static class MockAnalyzer extends AbstractMessageAnalyzer<AnalyzerResult> {
		
		@Override
		protected void store(AnalyzerResult result) {
		}

		@Override
		public AnalyzerResult generate() {
			return null;
		}

		@Override
		protected void process(MessageTree tree) {
			++s_count;
		}
	}

	public static class AnalyzerResult{
		
	}
	static class MockMessage extends AbstractMessage {
		public MockMessage() {
			super(null, null);
			
		}
		@Override
		public long getTimestamp() {
			return System.currentTimeMillis()-10*1000;
		}
		@Override
		public void complete() {
		}
	}
}
