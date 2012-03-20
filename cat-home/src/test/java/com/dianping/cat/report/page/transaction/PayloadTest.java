package com.dianping.cat.report.page.transaction;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.report.page.model.spi.ModelPeriod;

public class PayloadTest {
	private static final long ONE_HOUR = 3600 * 1000L;

	private void checkDate(Payload payload, int hours, long expectedDate, ModelPeriod expectedPeriod) {
		payload.setHours(hours);

		Assert.assertEquals(expectedDate, payload.getDate());
		Assert.assertEquals(expectedPeriod, payload.getPeriod());
	}

	@Test
	public void testDateNavigation() {
		Payload payload = new Payload();
		long timestamp = System.currentTimeMillis();
		long now = timestamp - timestamp % ONE_HOUR;

		checkDate(payload, 0, now, ModelPeriod.CURRENT);
		checkDate(payload, 1, now + ONE_HOUR, ModelPeriod.FUTURE);
		checkDate(payload, 2, now + 2 * ONE_HOUR, ModelPeriod.FUTURE);
		checkDate(payload, -1, now - ONE_HOUR, ModelPeriod.LAST);
		checkDate(payload, -2, now - 2 * ONE_HOUR, ModelPeriod.HISTORICAL);

		payload.setDate(now - ONE_HOUR);
		checkDate(payload, 2, now + ONE_HOUR, ModelPeriod.FUTURE);
		checkDate(payload, 1, now, ModelPeriod.CURRENT);
		checkDate(payload, 0, now - ONE_HOUR, ModelPeriod.LAST);
		checkDate(payload, -1, now - 2 * ONE_HOUR, ModelPeriod.HISTORICAL);
		checkDate(payload, -2, now - 3 * ONE_HOUR, ModelPeriod.HISTORICAL);

		payload.setDate(now - 2 * ONE_HOUR);
		checkDate(payload, 3, now + ONE_HOUR, ModelPeriod.FUTURE);
		checkDate(payload, 2, now, ModelPeriod.CURRENT);
		checkDate(payload, 1, now - ONE_HOUR, ModelPeriod.LAST);
		checkDate(payload, 0, now - 2 * ONE_HOUR, ModelPeriod.HISTORICAL);
		checkDate(payload, -1, now - 3 * ONE_HOUR, ModelPeriod.HISTORICAL);
		checkDate(payload, -2, now - 4 * ONE_HOUR, ModelPeriod.HISTORICAL);
	}
}
