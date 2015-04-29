package com.dianping.cat.service;

import org.junit.Test;

import com.dianping.cat.report.service.ModelPeriod;

import junit.framework.Assert;

public class ModelPeriodTest {

	@Test
	public void test() {
		long hour = 3600 * 1000L;
		long time = System.currentTimeMillis();
		long start = time - time % (3600 * 1000L);
		ModelPeriod period = ModelPeriod.getByTime(start);

		Assert.assertEquals(start, period.getStartTime());
		Assert.assertEquals(period.getStartTime() - hour, ModelPeriod.LAST.getStartTime());
		Assert.assertEquals(true, period.isCurrent());
		Assert.assertEquals(false, period.isHistorical());
		Assert.assertEquals(false, period.isLast());
		Assert.assertEquals(period, ModelPeriod.getByName(period.name(), period));
		Assert.assertEquals(period, ModelPeriod.getByName("other", period));
		Assert.assertEquals(period, ModelPeriod.getByTime(System.currentTimeMillis()));
		Assert.assertEquals(ModelPeriod.LAST, ModelPeriod.getByTime(System.currentTimeMillis() - hour));
		Assert.assertEquals(ModelPeriod.HISTORICAL, ModelPeriod.getByTime(System.currentTimeMillis() - hour * 2));
	}
}
