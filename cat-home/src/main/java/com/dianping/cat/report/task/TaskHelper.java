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
