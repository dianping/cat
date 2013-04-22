package com.dianping.cat.message;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.Cat;

@RunWith(JUnit4.class)
public class MetricTest {
	@Test
	public void testNormal() {
		Cat.logMetric("order", "sum", 123, "count", 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testException() {
		Cat.logMetric("order", "sum", 123, "count", 3, "key");
	}
}
