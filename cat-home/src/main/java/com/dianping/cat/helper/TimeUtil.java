package com.dianping.cat.helper;

import java.util.Calendar;
import java.util.Date;

public class TimeUtil {

	public static final long ONE_MINUTE = 60 * 1000L;

	public static final long ONE_HOUR = 60 * 60 * 1000L;

	public static final long ONE_DAY = 24 * ONE_HOUR;

	public static final long ONE_WEEK = 7 * ONE_DAY;

	public static Date getCurrentDay() {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

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

	// last week sarterday
	public static Date getLastWeek() {
		Date date = getCurrentWeek();
		return new Date(date.getTime() - 7 * ONE_DAY);
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

}
