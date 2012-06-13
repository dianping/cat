package com.dianping.cat.report.page.transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.report.page.model.spi.ModelPeriod;

public class PayloadTest {
	private static final long ONE_HOUR = 3600 * 1000L;

	private static final long ONE_DAY = 24 * ONE_HOUR;

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");

	private void checkDate(Payload payload, int hours, long expectedDate, ModelPeriod expectedPeriod) {
		payload.setStep(hours);

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

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		String currentHour = sdf.format(new Date(now - ONE_HOUR));

		payload.setDate(currentHour);
		checkDate(payload, 2, now + ONE_HOUR, ModelPeriod.FUTURE);
		checkDate(payload, 1, now, ModelPeriod.CURRENT);
		checkDate(payload, 0, now - ONE_HOUR, ModelPeriod.LAST);
		checkDate(payload, -1, now - 2 * ONE_HOUR, ModelPeriod.HISTORICAL);
		checkDate(payload, -2, now - 3 * ONE_HOUR, ModelPeriod.HISTORICAL);

		currentHour = sdf.format(new Date(now - 2 * ONE_HOUR));
		payload.setDate(currentHour);
		checkDate(payload, 3, now + ONE_HOUR, ModelPeriod.FUTURE);
		checkDate(payload, 2, now, ModelPeriod.CURRENT);
		checkDate(payload, 1, now - ONE_HOUR, ModelPeriod.LAST);
		checkDate(payload, 0, now - 2 * ONE_HOUR, ModelPeriod.HISTORICAL);
		checkDate(payload, -1, now - 3 * ONE_HOUR, ModelPeriod.HISTORICAL);
		checkDate(payload, -2, now - 4 * ONE_HOUR, ModelPeriod.HISTORICAL);
	}

	public void checkDate(String str, Date date) {
		Assert.assertEquals(str, sdf.format(date));
	}

	@Test
	public void testHistoryDayNav() {
		Payload payload = new Payload();
		payload.setReportType("day");
		Date date = new Date();
		long temp = date.getTime() - date.getTime() % (ONE_HOUR);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(temp));
		cal.set(Calendar.HOUR_OF_DAY, 0);

		Date input = new Date(temp);

		temp = cal.getTimeInMillis();
		Date lastTwoDay = new Date(temp - 2 * ONE_DAY);
		Date lastOneDay = new Date(temp - ONE_DAY);
		Date currentDay = new Date(temp);
		String lastTwo = sdf.format(lastTwoDay);
		String lastOne = sdf.format(lastOneDay);
		String current = sdf.format(currentDay);
		payload.setDate(sdf.format(input));

		payload.setStep(-1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, payload.getHistoryEndDate());

		payload.computeStartDate();
		checkDate(lastTwo, payload.getHistoryStartDate());
		checkDate(lastOne, payload.getHistoryEndDate());

		payload.setStep(1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, payload.getHistoryEndDate());

		payload.setStep(1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, payload.getHistoryEndDate());
		
		payload.setStep(1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, payload.getHistoryEndDate());
	}

	@Test
	public void testHistoryWeekNav() {
		Payload payload = new Payload();

		payload.setReportType("week");

		Date date = new Date();
		long temp = date.getTime() - date.getTime() % (ONE_HOUR);
		Date input = new Date(temp);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date(temp));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		temp = cal.getTimeInMillis();

		int weekOfDay = cal.get(Calendar.DAY_OF_WEEK);
		temp = temp - 24 * (weekOfDay - 1) * ONE_HOUR;
		temp = temp+24*ONE_HOUR;
		Date lastTwoWeek = new Date(temp - 7 * 2 * ONE_DAY);
		Date lastOneWeek = new Date(temp - 7 * ONE_DAY);
		Date currentWeek = new Date(temp);
		String lastTwo = sdf.format(lastTwoWeek);
		String lastOne = sdf.format(lastOneWeek);
		String current = sdf.format(currentWeek);
		payload.setDate(sdf.format(input));

		payload.setStep(-1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(sdf.format(new Date(lastOneWeek.getTime() + 8 * ONE_DAY)), payload.getHistoryEndDate());

		payload.computeStartDate();
		checkDate(lastTwo, payload.getHistoryStartDate());
		checkDate(sdf.format(new Date(lastTwoWeek.getTime() + 8 * ONE_DAY)), payload.getHistoryEndDate());

		payload.setStep(1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(sdf.format(new Date(lastOneWeek.getTime() + 8 * ONE_DAY)), payload.getHistoryEndDate());

		payload.computeStartDate();
		checkDate(current, payload.getHistoryStartDate());
		checkDate(sdf.format(currentWeek.getTime() + 8 * ONE_DAY), payload.getHistoryEndDate());

		payload.computeStartDate();
		checkDate(current, payload.getHistoryStartDate());
		checkDate(sdf.format(currentWeek.getTime() + 8 * ONE_DAY), payload.getHistoryEndDate());
	}

	@Test
	public void testHistoryMonthNav() {
		Payload payload = new Payload();
		payload.setReportType("month");

		Date date = new Date();
		long temp = date.getTime() - date.getTime() % (ONE_HOUR);
		Date input = new Date(temp);
		String lastTwo = "2012040100";
		String lastOne = "2012050100";
		String current = "2012060100";
		
		payload.setDate(sdf.format(input));

		payload.setStep(-1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, payload.getHistoryEndDate());

		payload.computeStartDate();
		checkDate(lastTwo, payload.getHistoryStartDate());
		checkDate(lastOne, payload.getHistoryEndDate());

		payload.setStep(1);
		payload.computeStartDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, payload.getHistoryEndDate());
	}
}
