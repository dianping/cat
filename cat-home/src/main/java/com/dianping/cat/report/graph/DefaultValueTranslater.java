package com.dianping.cat.report.graph;

public class DefaultValueTranslater implements ValueTranslater {
	@Override
	public int getMaxValue(double[] values) {
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
		int maxValue = (int) Math.pow(10, Math.ceil(maxLog));

		while (maxValue > max * 2) {
			maxValue = maxValue / 2;
		}
		
		return maxValue;
	}

	@Override
	public int[] translate(int height, int maxValue, double[] values) {
		int len = values.length;
		int[] result = new int[len];

		for (int i = 0; i < len; i++) {
			double value = values[i];

			result[i] = (int) (value * height / maxValue);
		}

		return result;
	}
}
