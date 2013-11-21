package com.dianping.cat.service;

import org.junit.Test;

import junit.framework.Assert;

public class ModelPeriodTest {

	@Test
	public void test(){
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		ModelPeriod period = ModelPeriod.getByTime(start);
		
		Assert.assertEquals(start,period.getStartTime());
		Assert.assertEquals(true,period.isCurrent());
		Assert.assertEquals(false,period.isFuture());
		Assert.assertEquals(false,period.isHistorical());
		Assert.assertEquals(false,period.isLast());
		Assert.assertEquals(period, ModelPeriod.getByName(period.name(), period));
	}
}
