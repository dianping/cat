package com.dianping.cat.message.pipeline;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.MetricBag;
import com.dianping.cat.message.context.MetricContext;

public class MetricAggregatorTest extends ComponentTestCase {
	private AtomicInteger m_count = new AtomicInteger();

	private StringBuilder m_sb = new StringBuilder();

	@Before
	public void before() throws Exception {
		context().registerComponent(MessageHandler.class, new CounterHandler());
	}

	@Test
	public void test() throws InterruptedException {
		Cat.logMetricForCount("count");
		Cat.logMetricForSum("sum", 100);
		Cat.logMetricForSum("sum", 100);
		Cat.logMetricForDuration("duration", 200);
		Cat.logMetricForDuration("duration", 200);
		Cat.logMetricForDuration("duration", 200);

		// trigger metric aggregation
		lookup(MessagePipeline.class).headContext(MetricContext.TICK).fireMessage(MetricContext.TICK);

		Assert.assertEquals(1, m_count.get());

		Assert.assertEquals(true, m_sb.length() > 150);
	}

	private class CounterHandler implements MessageHandler {
		@Override
		public int getOrder() {
			return 290;
		}

		@Override
		public void handleMessage(MessageHandlerContext ctx, Object msg) {
			if (msg instanceof MetricBag) {
				m_count.incrementAndGet();
				m_sb.append(msg);
			}
		}
	}
}
