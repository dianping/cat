package com.dianping.cat.report.alert;

import junit.framework.Assert;

import org.junit.Test;

public class ExtractDataTest {
	private double[] extract(double[] lastHourValues, double[] currentHourValues, int maxMinute, int alreadyMinute) {
		int lastLength = maxMinute - alreadyMinute - 1;
		double[] result = new double[maxMinute];

		for (int i = 0; i < lastLength; i++) {
			result[i] = lastHourValues[60 - lastLength + i];
		}
		for (int i = lastLength; i < maxMinute; i++) {
			result[i] = currentHourValues[i - lastLength];
		}
		return result;
	}

	private double[] extract(double[] values, int maxMinute, int alreadyMinute) {
		double[] result = new double[maxMinute];

		for (int i = 0; i < maxMinute; i++) {
			result[i] = values[alreadyMinute + 1 - maxMinute + i];
		}
		return result;
	}

	@Test
	public void testCurrentData() {
		double[] values = new double[60];
		for (int i = 0; i < 10; i++) {
			values[i] = i;
		}

		double[] result = extract(values, 5, 9);
		Assert.assertEquals(5.0, result[0]);
	}

	@Test
	public void testLastData() {
		double[] values = new double[60];
		for (int i = 0; i < 60; i++) {
			values[i] = i;
		}

		double[] result = extract(values, 5, 59);
		Assert.assertEquals(55.0, result[0]);
	}

	@Test
	public void testCombineData() {
		double[] lastValues = new double[60];
		for (int i = 0; i < 60; i++) {
			lastValues[i] = i;
		}

		double[] currentValues = new double[10];
		for (int i = 0; i < 10; i++) {
			currentValues[i] = i;
		}

		double[] result = extract(lastValues, currentValues, 15, 9);
		Assert.assertEquals(55.0, result[0]);
	}
}
