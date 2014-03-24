package com.dianping.cat.report.task.metric;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.Point;

public class MetricPointParser {

	private static final int POINT_NUMBER = 60;

	public double[] buildHourlyData(MetricItem item, MetricType type) {
		double[] result = new double[POINT_NUMBER];
		Map<Integer, Point> map = item.getPoints();

		for (Entry<Integer, Point> entry : map.entrySet()) {
			Integer minute = entry.getKey();
			Point point = entry.getValue();

			if (type == MetricType.AVG) {
				result[minute] = point.getAvg();
			} else if (type == MetricType.COUNT) {
				result[minute] = (double) point.getCount();
			} else if (type == MetricType.SUM) {
				result[minute] = point.getSum();
			}
		}
		return result;
	}

	public double[] buildDailyData(List<MetricItem> items, MetricType type) {
		int size = items.size();
		double[] values = new double[24 * POINT_NUMBER];

		for (int i = 0; i < 24 * POINT_NUMBER; i++) {
			values[i] = -1;
		}
		for (int hour = 0; hour < size; hour++) {
			MetricItem item = items.get(hour);
			try {
				double[] oneHourValues = buildHourlyData(item, type);

				for (int minute = 0; minute < 60; minute++) {
					int index = hour * 60 + minute;
					values[index] = oneHourValues[minute];
				}
			} catch (Exception e) {
				continue;
			}
		}
		return values;
	}

}
