package com.dianping.cat.report.baseline.impl;

import java.util.List;

import com.dianping.cat.report.baseline.BaselineCreator;

public class DefaultBaselineCreator implements BaselineCreator {

	@Override
	public double[] createBaseLine(List<double[]> valueList, List<Double> weights, int number) {
		double[] result = new double[number];

		for (int i = 0; i < number; i++) {
			double totalValue = 0;
			double totalWeight = 0;

			for (int j = 0; j < weights.size(); j++) {
				double[] values = valueList.get(j);
				double weight = weights.get(j);
				
				if (values[i] > 0) {
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
		return denoise(result, 6);
	}

	public double[] denoise(double[] data, int mixNumber) {
		if (mixNumber <= 2) {
			return data;
		}
		int number = data.length;
		double[] result = new double[number];
		boolean first = true;
		boolean last = false;

		for (int i = 0; i < number - mixNumber; i++) {
			if (i == number - mixNumber - 1) {
				last = true;
			}

			double min = Double.MAX_VALUE;
			double max = 0;
			double sum = 0;

			for (int j = 0; j < mixNumber; j++) {
				int position = i + j;

				if (data[position] > max) {
					max = data[position];
				}
				if (data[position] < min) {
					min = data[position];
				}
				sum = sum + data[position];
			}
			double avg = (sum - max - min) / (mixNumber - 2);
			result[i + mixNumber / 2] = avg;

			if (first) {
				first = false;

				for (int k = 0; k < mixNumber / 2; k++) {
					result[k] = avg;
				}
			}
			if (last) {
				for (int k = i + mixNumber / 2 + 1; k < number; k++) {
					result[k] = avg;
				}
			}
		}

		return result;
	}
}
