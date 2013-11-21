package com.dianping.cat.service;

import junit.framework.Assert;

import org.junit.Test;

public class ModelRequestTest {

	@Test
	public void test() {
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		ModelRequest request = new ModelRequest("Cat", start);
		String str = "test";

		request.setProperty(str, str);

		Assert.assertEquals(ModelPeriod.CURRENT, request.getPeriod());
		Assert.assertEquals(str, request.getProperty(str));
		Assert.assertEquals(start, request.getStartTime());
		Assert.assertEquals("ModelRequest[domain=Cat, period=CURRENT, properties={test=test}]", request.toString());

	}
}
