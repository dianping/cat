package com.dianping.cat.message.pipeline;

import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.MetricBag;

public class MetricAggregatorTest extends ComponentTestCase {
	private int m_count;

	private StringBuilder m_sb = new StringBuilder();

	@Before
	public void before() throws Exception {
		Cat.getBootstrap().testMode();

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

		// wait for a tick to complete
		TimeUnit.MILLISECONDS.sleep(1500);

		Assert.assertEquals(1, m_count);
		Assert.assertEquals(193, m_sb.length());
	}

	private class CounterHandler implements MessageHandler {
		@Override
		public int getOrder() {
			return 290;
		}

		@Override
		public void handleMessage(MessageHandlerContext ctx, Object msg) {
			if (msg instanceof MetricBag) {
				m_count++;
				m_sb.append(msg);
			}
		}
	}
}
