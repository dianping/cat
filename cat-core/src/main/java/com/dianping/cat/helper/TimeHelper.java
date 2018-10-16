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
package com.dianping.cat.helper;

import java.util.Calendar;
import java.util.Date;

public class TimeHelper {

	public static final long ONE_SECOND = 1000L;

	public static final long ONE_MINUTE = 60 * 1000L;

	public static final long ONE_HOUR = 60 * 60 * 1000L;

	public static final long ONE_DAY = 24 * ONE_HOUR;

	public static final long ONE_WEEK = 7 * ONE_DAY;

	public static Date addDays(Date date, int day) {
		Calendar cal = Calendar.getInstance();

		cal.setTime(date);
		cal.add(Calendar.DAY_OF_MONTH, day);

		return cal.getTime();
	}

	public static Date getCurrentDay() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public static Date getCurrentDay(int index) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_MONTH, index);

		return cal.getTime();
	}

	public static Date getCurrentDay(long timestamp) {
		return getCurrentDay(timestamp, 0);
	}

	public static Date getCurrentDay(long timestamp, int index) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_MONTH, index);
		return cal.getTime();
	}

	public static Date getCurrentHour() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public static Date getCurrentHour(int index) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.HOUR_OF_DAY, index);

		return cal.getTime();
	}

	public static Date getCurrentMinute() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public static Date getCurrentMinute(int index) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MINUTE, index);

		return cal.getTime();
	}

	public static Date getCurrentMonth() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	// get lastest sarterday
	public static Date getCurrentWeek() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

		if (dayOfWeek == 7) {
			return cal.getTime();
		} else {
			cal.add(Calendar.DATE, -dayOfWeek);
		}
		return cal.getTime();
	}

	public static Date getLastMonth() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.DAY_OF_MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}

	public static String getMinuteStr() {
		int minute = Calendar.getInstance().get(Calendar.MINUTE);
		String minuteStr = String.valueOf(minute);

		if (minute < 10) {
			minuteStr = '0' + minuteStr;
		}

		return "M" + minuteStr;
	}

	public static Date getStepSecond(int step) {
		long current = System.currentTimeMillis();
		long gap = current % ONE_MINUTE;
		long minute = current - gap;
		int index = (int) gap / (int) (step * ONE_SECOND);

		return new Date(minute + index * step * ONE_SECOND);
	}

	public static Date getYesterday() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.DAY_OF_MONTH, -1);

		return cal.getTime();
	}

	public static boolean sleepToNextMinute() {
		try {
			long current = System.currentTimeMillis();

			Thread.sleep(ONE_MINUTE - current % ONE_MINUTE + 500);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	public static boolean sleepToNextMinute(long overTime) {
		try {
			long current = System.currentTimeMillis();

			Thread.sleep(ONE_MINUTE - current % ONE_MINUTE + overTime);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

}
