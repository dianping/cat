package com.dianping.cat.report.graph;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.report.graph.svg.ValueTranslater;

@RunWith(JUnit4.class)
public class ValueTranslaterTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		check(1000, 123, 456, 247, 473, 976, 236);
		check(5, 1, 3, 5);
		check(0.5, 0.1, 0.3, 0.4);
		check(0.25, 0.01, 0.2, 0.1);
		
	}

	void check(double expected, double... values) throws Exception {
		ValueTranslater translater = lookup(ValueTranslater.class);

		Assert.assertEquals(expected, translater.getMaxValue(values));
	}
}
