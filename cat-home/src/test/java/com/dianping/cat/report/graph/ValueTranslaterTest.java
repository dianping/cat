package com.dianping.cat.report.graph;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class ValueTranslaterTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		ValueTranslater translater = lookup(ValueTranslater.class);
		double[] values = { 123, 456, 247, 473, 976, 236 };

		Assert.assertEquals(1000, translater.getMaxValue(values));
	}
}
