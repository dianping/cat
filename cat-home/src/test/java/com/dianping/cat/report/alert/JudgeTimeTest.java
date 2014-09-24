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
