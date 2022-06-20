package com.dianping.cat.message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.Metric.Kind;
import com.dianping.cat.message.pipeline.MessageHandler;
import com.dianping.cat.message.pipeline.MessageHandlerAdaptor;
import com.dianping.cat.message.pipeline.MessageHandlerContext;

public class MetricTest extends ComponentTestCase {
	@After
	public void after() {
		MetricAssert.reset();
	}

	@Test
	public void testCount() {
		Cat.logMetricForCount("metric");

		MetricAssert.name("metric").kind(Kind.COUNT).count(1);
	}

	@Test
	public void testCounts() {
		Cat.logMetricForCount("metric");
		Cat.logMetricForCount("metric", 2);
		Cat.logMetricForCount("metric", 3);

		MetricAssert.name("metric").kind(Kind.COUNT).count(6);
	}

	@Test
	public void testSum() {
		Cat.logMetricForSum("metric", 100);

		MetricAssert.name("metric").kind(Kind.SUM).count(1).sum(100);
	}

	@Test
	public void testSums() {
		Cat.logMetricForSum("metric", 100);
		Cat.logMetricForSum("metric", 200);
		Cat.logMetricForSum("metric", 300);

		MetricAssert.name("metric").kind(Kind.SUM).count(3).sum(600);
	}

	@Test
	public void testDuration() {
		Cat.logMetricForDuration("metric", 200);

		MetricAssert.name("metric").kind(Kind.DURATION).count(1).duration(200);
	}

	@Test
	public void testDurations() {
		Cat.logMetricForDuration("metric", 200);
		Cat.logMetricForDuration("metric", 300);
		Cat.logMetricForDuration("metric", 400);

		MetricAssert.name("metric").kind(Kind.DURATION).count(3).duration(900);
	}

	@Before
	public void before() throws Exception {
		Cat.getBootstrap().testMode();

		context().registerComponent(MessageHandler.class, new MockMessageHandler());
	}

	private class MockMessageHandler extends MessageHandlerAdaptor {
		@Override
		public int getOrder() {
			return 0;
		}

		@Override
		protected void handleMetric(MessageHandlerContext ctx, Metric metric) {
			MetricAssert.newMetric(metric);
		}
	}
}
