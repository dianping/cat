/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.report.page.transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;
import org.junit.Test;

import com.dianping.cat.report.service.ModelPeriod;

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
		checkDate(payload, -1, now - ONE_HOUR, ModelPeriod.LAST);
		checkDate(payload, -2, now - 2 * ONE_HOUR, ModelPeriod.HISTORICAL);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		String currentHour = sdf.format(new Date(now - ONE_HOUR));

		payload.setDate(currentHour);
		checkDate(payload, 2, now, ModelPeriod.CURRENT);
		checkDate(payload, 1, now, ModelPeriod.CURRENT);
		checkDate(payload, 0, now - ONE_HOUR, ModelPeriod.LAST);
		checkDate(payload, -1, now - 2 * ONE_HOUR, ModelPeriod.HISTORICAL);
		checkDate(payload, -2, now - 3 * ONE_HOUR, ModelPeriod.HISTORICAL);

		currentHour = sdf.format(new Date(now - 2 * ONE_HOUR));
		payload.setDate(currentHour);
		checkDate(payload, 3, now, ModelPeriod.CURRENT);
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
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, adjustEndDate(payload.getHistoryEndDate()));

		payload.computeHistoryDate();
		checkDate(lastTwo, payload.getHistoryStartDate());
		checkDate(lastOne, adjustEndDate(payload.getHistoryEndDate()));

		payload.setStep(1);
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, adjustEndDate(payload.getHistoryEndDate()));

		payload.setStep(1);
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, adjustEndDate(payload.getHistoryEndDate()));

		payload.setStep(1);
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, adjustEndDate(payload.getHistoryEndDate()));
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

		int weekOfDay = cal.get(Calendar.DAY_OF_WEEK) % 7;
		temp = temp - 24 * (weekOfDay) * ONE_HOUR;
		if (temp > System.currentTimeMillis()) {
			temp = temp - 7 * ONE_DAY;
		}
		Date lastTwoWeek = new Date(temp - 7 * 2 * ONE_DAY);
		Date lastOneWeek = new Date(temp - 7 * ONE_DAY);
		Date currentWeek = new Date(temp);
		String lastTwo = sdf.format(lastTwoWeek);
		String lastOne = sdf.format(lastOneWeek);
		String current = sdf.format(currentWeek);

		payload.setDate(sdf.format(input));

		payload.setStep(-1);
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(sdf.format(new Date(lastOneWeek.getTime() + 7 * ONE_DAY)), adjustEndDate(payload.getHistoryEndDate()));

		payload.computeHistoryDate();
		checkDate(lastTwo, payload.getHistoryStartDate());
		checkDate(sdf.format(new Date(lastTwoWeek.getTime() + 7 * ONE_DAY)), adjustEndDate(payload.getHistoryEndDate()));

		payload.setStep(1);
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(sdf.format(new Date(lastOneWeek.getTime() + 7 * ONE_DAY)), adjustEndDate(payload.getHistoryEndDate()));

		payload.computeHistoryDate();
		payload.setStep(1);
		checkDate(current, payload.getHistoryStartDate());
		checkDate(sdf.format(currentWeek.getTime() + 7 * ONE_DAY), adjustEndDate(payload.getHistoryEndDate()));

		payload.computeHistoryDate();
		checkDate(current, payload.getHistoryStartDate());
		checkDate(sdf.format(currentWeek.getTime() + 7 * ONE_DAY), adjustEndDate(payload.getHistoryEndDate()));
	}

	@Test
	public void testHistoryMonthNav() {
		Payload payload = new Payload();
		payload.setReportType("month");

		Date date = new Date();
		long temp = date.getTime() - date.getTime() % (ONE_HOUR);
		Date input = new Date(temp);

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(temp);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		String current = sdf.format(cal.getTime());
		cal.add(Calendar.MONTH, -1);
		String lastOne = sdf.format(cal.getTime());
		cal.add(Calendar.MONTH, -1);
		String lastTwo = sdf.format(cal.getTime());

		payload.setDate(sdf.format(input));

		payload.setStep(-1);
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, adjustEndDate(payload.getHistoryEndDate()));

		payload.computeHistoryDate();
		checkDate(lastTwo, payload.getHistoryStartDate());
		checkDate(lastOne, adjustEndDate(payload.getHistoryEndDate()));

		payload.setStep(1);
		payload.computeHistoryDate();
		checkDate(lastOne, payload.getHistoryStartDate());
		checkDate(current, adjustEndDate(payload.getHistoryEndDate()));
	}

	private Date adjustEndDate(Date date) {
		return new Date(date.getTime());
	}
}
