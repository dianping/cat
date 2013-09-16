package com.dianping.cat.report.baseline.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.dianping.cat.report.baseline.BaselineCreator;

public class DefaultBaselineCreator implements BaselineCreator {

	private static final double NORMAL_DATA_LOWER_LIMIT = 0.2;

	private static final double NORMAL_DATA_UPPER_LIMIT = 5;

	private static final double MIN_NOISY_DATA = 50;

	@Override
	public double[] createBaseLine(List<double[]> valueList, List<Double> weights, Set<Integer> omittedPoints, int number) {

		double[] result = new double[number];

		denoise(valueList, omittedPoints, number);
		for (int i = 0; i < number; i++) {
			double totalValue = 0;
			double totalWeight = 0;

			for (int j = 0; j < weights.size(); j++) {
				int index = j * number + i;
				double[] values = valueList.get(j);
				double weight = weights.get(j);
				if (!omittedPoints.contains(index)) {
					totalValue += values[i] * weight;
					totalWeight += weight;
				}
			}
			if (totalWeight == 0) {
				if (i != 0) {
					result[i] = result[i - 1];
				} else {
					result[i] = 0;
				}
			} else {
				result[i] = totalValue / totalWeight;
			}
		}
		return result;
	}

	private void denoise(List<double[]> valueList, Set<Integer> omittedPoints, int number) {
		int i = 0;
		for (double[] values : valueList) {
			for (int j = 0; j < number; j++) {
				int index = i * number + j;
				if (!omittedPoints.contains(index) && !checkData(valueList, j, values[j])) {
					omittedPoints.add(index);
				}
			}
			i++;
		}
	}

	private boolean checkData(List<double[]> list, int n, double data) {
		if (data < 0) {
			return false;
		}

		List<Double> oneMinuteDataList = new ArrayList<Double>();
		for (double[] values : list) {
			if (values[n] > 0) {
				oneMinuteDataList.add(values[n]);
			}
		}
		Collections.sort(oneMinuteDataList);

		double middleValue = 0;
		int size = oneMinuteDataList.size();
		if (size == 0) {
			middleValue = -1;
		} else if (size % 2 == 1) {
			middleValue = oneMinuteDataList.get((oneMinuteDataList.size() - 1) / 2);
		} else {
			middleValue = oneMinuteDataList.get(oneMinuteDataList.size() / 2) / 2
			      + oneMinuteDataList.get(oneMinuteDataList.size() / 2 - 1) / 2;
		}
		if (middleValue > MIN_NOISY_DATA
		      && (data / middleValue > NORMAL_DATA_UPPER_LIMIT || data / middleValue < NORMAL_DATA_LOWER_LIMIT)) {
			return false;
		}
		return true;
	}
}
