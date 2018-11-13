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
package com.dianping.cat.report.alert;

import java.util.Calendar;

import junit.framework.Assert;
import org.junit.Test;

public class JudgeTimeTest {

	@Test
	public void testStartTime() {
		Calendar cal = Calendar.getInstance();

		Assert.assertFalse(compareTime("25:00", cal, true));
	}

	@Test
	public void testEndTime() {
		Calendar cal = Calendar.getInstance();

		Assert.assertTrue(compareTime("25:00", cal, false));
	}

	private boolean compareTime(String timeStr, Calendar currentCal, boolean isStartTime) {
		String[] times = timeStr.split(":");
		int hour = Integer.parseInt(times[0]);
		int minute = Integer.parseInt(times[1]);
		int currentHour = currentCal.get(Calendar.HOUR_OF_DAY);
		int currentMinute = currentCal.get(Calendar.MINUTE);

		if (currentHour == hour) {
			if (currentMinute == minute) {
				return true;
			} else {
				return (currentMinute > minute) == isStartTime;
			}
		} else {
			return (currentHour > hour) == isStartTime;
		}
	}

}
