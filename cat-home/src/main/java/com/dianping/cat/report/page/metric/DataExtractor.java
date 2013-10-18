package com.dianping.cat.report.page.metric;

import java.util.List;

public class DataExtractor {

	private int m_timeRange;

	private int m_interval;

	private static final int MINUTE = 60;

	public DataExtractor(int timeRange, int interval) {
		super();
		this.m_timeRange = timeRange;
		this.m_interval = interval;
	}

	public double[] extract(List<double[]> datas, int offset) {
		int pointNumber = (m_timeRange * MINUTE) / m_interval;
		double[] result = new double[pointNumber];

		if (datas == null) {
			return result;
		}
		int size = datas.size();
		double[] hourData = null;
		int length = 0;

		for (int i = 0, j = -1; i < pointNumber; i++) {
			int insideOffset = i * m_interval + offset;
			while (length <= insideOffset) {
				insideOffset = insideOffset - length;
				offset = offset - length;
				j++;
				if (j >= size) {
					return result;
				}
				hourData = datas.get(j);
				if (hourData == null) {
					length = 0;
					continue;
				}
				length = hourData.length;
			}
			result[i] = avgOfArray(hourData, insideOffset, m_interval);
		}

		return result;
	}

	private double avgOfArray(double[] values, int index, int interval) {

		double result = 0;

		for (int i = index; i < index + interval; i++) {
			if (values[i] >= 0) {
				result += values[i];
			}
		}
		return result / interval;
	}

	// private double sumOfArray(double[] values, int index, int interval) {
	//
	// double result = 0;
	//
	// for (int i = index; i < index + interval; i++) {
	// if (values[i] >= 0) {
	// result += values[i];
	// }
	// }
	// return result;
	// }
}
