package com.dianping.cat.report.page.metric.task;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.report.alert.MetricType;

public class MetricPointParser {

	private static final int POINT_NUMBER = 60;

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

	public double[] buildHourlyData(MetricItem item, MetricType type) {
		double[] result = new double[POINT_NUMBER];
		Map<Integer, Segment> map = item.getSegments();

		for (Entry<Integer, Segment> entry : map.entrySet()) {
			Integer minute = entry.getKey();
			Segment seg = entry.getValue();

			if (type == MetricType.AVG) {
				result[minute] = seg.getAvg();
			} else if (type == MetricType.COUNT) {
				result[minute] = (double) seg.getCount();
			} else if (type == MetricType.SUM) {
				result[minute] = seg.getSum();
			}
		}
		return result;
	}

}
