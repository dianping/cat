/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.Calendar;
import java.util.Date;

public class TaskHelper {

	public static String join(double[] array, char separator) {
		return join(array, separator, 0, array.length - 1);
	}

	public static String join(double[] array, char separator, int startIndex, int endIndex) {
		if (array == null) {
			return null;
		}
		int bufSize = (endIndex - startIndex);
		if (bufSize <= 0) {
			return "";
		}

		bufSize *= 4;
		StringBuilder buf = new StringBuilder(bufSize);

		for (int i = startIndex; i <= endIndex; i++) {
			buf.append(array[i]);
			if (i < endIndex) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static String join(int[] array, char separator) {
		return join(array, separator, 0, array.length - 1);
	}

	public static String join(int[] array, char separator, int startIndex, int endIndex) {
		if (array == null) {
			return null;
		}
		int bufSize = (endIndex - startIndex);
		if (bufSize <= 0) {
			return "";
		}

		bufSize *= 4;
		StringBuilder buf = new StringBuilder(bufSize);

		for (int i = startIndex; i <= endIndex; i++) {
			buf.append(array[i]);
			if (i < endIndex) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static String join(Number[] array, char separator) {
		return join(array, separator, 0, array.length - 1);
	}

	public static String join(Number[] array, char separator, int startIndex, int endIndex) {
		if (array == null) {
			return null;
		}
		int bufSize = (endIndex - startIndex);
		if (bufSize <= 0) {
			return "";
		}

		bufSize *= 4;
		StringBuilder buf = new StringBuilder(bufSize);

		for (int i = startIndex; i <= endIndex; i++) {
			buf.append(array[i]);
			if (i < endIndex) {
				buf.append(separator);
			}
		}
		return buf.toString();
	}

	public static Date nextTaskTime() {
		Calendar cal = Calendar.getInstance();
		final int startFindMin = 10;
		cal.set(Calendar.MINUTE, startFindMin);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (cal.get(Calendar.MINUTE) >= startFindMin) {
			cal.add(Calendar.HOUR, 1);// timeout, waiting for next hour
		}
		return cal.getTime();
	}

	public static Date startDateOfNextTask(Date currentDate) {
		long day = 24 * 60 * 60 * 1000L;
		long nineMissecond = 9 * 60 * 1000L;
		Date dayStart = TaskHelper.todayZero(currentDate);

		if (currentDate.getTime() - dayStart.getTime() > nineMissecond) {
			return new Date(dayStart.getTime() + day + nineMissecond);
		} else {
			return new Date(dayStart.getTime() + nineMissecond);
		}
	}

	public static Date todayZero(Date reportPeriod) {
		if (reportPeriod == null) {
			reportPeriod = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date tomorrowZero(Date reportPeriod) {
		if (reportPeriod == null) {
			reportPeriod = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date yesterdayZero(Date reportPeriod) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reportPeriod);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
