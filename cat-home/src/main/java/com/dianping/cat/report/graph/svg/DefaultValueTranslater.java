package com.dianping.cat.report.graph.svg;

public class DefaultValueTranslater implements ValueTranslater {
	@Override
	public double getMaxValue(double[] values) {
		double min = Integer.MAX_VALUE;
		double max = Integer.MIN_VALUE;
		int len = values.length;

		for (int i = 0; i < len; i++) {
			double value = values[i];

			if (value < min) {
				min = value;
			}

			if (value > max) {
				max = value;
			}
		}

		double maxLog = Math.log10(max);
		double maxValue = Math.pow(10, Math.ceil(maxLog));

		if (max > 0) {
			while (maxValue >= max * 2) {
				maxValue = maxValue / 2;
			}
		}

		return maxValue;
	}

	@Override
	public int[] translate(int height, double maxValue, double[] values) {
		int len = values.length;
		int[] result = new int[len];

		for (int i = 0; i < len; i++) {
			double value = values[i];
			double temp = value * height / maxValue;

			if (temp > 0 && temp < 1) {
				temp = 1;
			}
			result[i] = (int) temp;
		}

		return result;
	}
}
