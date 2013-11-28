package com.dianping.cat.service;

import junit.framework.Assert;

import org.junit.Test;

public class ModelRequestTest {

	@Test
	public void test() {
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		String domain = "Cat";
		String str = "test";
		ModelRequest request = new ModelRequest(domain, start);

		request.setProperty(str, str);

		Assert.assertEquals(ModelPeriod.CURRENT, request.getPeriod());
		Assert.assertEquals(str, request.getProperty(str));
		Assert.assertEquals("{test=test}", request.getProperties().toString());
		Assert.assertEquals(start, request.getStartTime());
		Assert.assertEquals(domain, request.getDomain());
		Assert.assertEquals("ModelRequest[domain=Cat, period=CURRENT, properties={test=test}]", request.toString());

	}
}
