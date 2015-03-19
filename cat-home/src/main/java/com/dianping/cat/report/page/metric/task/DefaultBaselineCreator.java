package com.dianping.cat.report.page.metric.task;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DefaultBaselineCreator implements BaselineCreator {

	public double computeAvg(List<Double> data) {
		Collections.sort(data);
		int length = data.size();
		double value = 0;
		int middle = length / 2;

		if (length % 2 == 0) {
			value = (data.get(middle - 1) + data.get(middle)) / 2;
		} else {
			value = data.get(middle);
		}

		int size = 0;
		double sum = 0;

		for (double d : data) {
			if (d > value * 3 || d < value / 3) {
				continue;
			} else {
				size++;
				sum = sum + d;
			}
		}
		return sum / size;
	}

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
		return denoise(result, 30);
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

			List<Double> mixNumbers = new ArrayList<Double>();
			for (int j = 0; j < mixNumber; j++) {
				int position = i + j;

				mixNumbers.add(data[position]);
			}
			double avg = computeAvg(mixNumbers);
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
