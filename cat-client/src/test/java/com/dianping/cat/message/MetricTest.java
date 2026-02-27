package com.dianping.cat.message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.Cat;
import com.dianping.cat.ComponentTestCase;
import com.dianping.cat.message.Metric.Kind;

public class MetricTest extends ComponentTestCase {
	@After
	public void after() {
		MetricAssert.reset();
	}

	@Before
	public void before() throws Exception {
		Cat.getBootstrap().testMode();

		MetricAssert.intercept(context());
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
}
