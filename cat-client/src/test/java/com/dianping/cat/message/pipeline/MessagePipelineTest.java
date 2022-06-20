package com.dianping.cat.message.pipeline;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.internal.DefaultMessageTree;

public class MessagePipelineTest extends ComponentTestCase {
	private int m_count;

	@Test
	public void test() {
		context().registerComponent(MessageHandler.class, new CounterHandler());

		MessagePipeline pipeline = lookup(MessagePipeline.class);
		DefaultMessageTree tree = new DefaultMessageTree();
		MessageHandlerContext ctx = pipeline.headContext(tree);

		ctx.fireMessage(tree);
		ctx.fireMessage(tree);

		Assert.assertEquals(2, m_count);
	}

	private class CounterHandler implements MessageHandler {
		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		public void handleMessage(MessageHandlerContext ctx, Object msg) {
			m_count++;
		}
	}
}
