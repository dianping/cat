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

	public static Date nextMonthStart(Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.MONTH, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		return cal.getTime();
	}

	public static Date thisHour(Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date todayZero(Date period) {
		if (period == null) {
			period = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date tomorrowZero(Date period) {
		if (period == null) {
			period = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.DAY_OF_YEAR, 1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public static Date yesterdayZero(Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.DAY_OF_YEAR, -1);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
}
