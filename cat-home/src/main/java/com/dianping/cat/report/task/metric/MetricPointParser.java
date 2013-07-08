package com.dianping.cat.report.task.metric;

import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.Point;

public class MetricPointParser {

	private static final int POINT_NUMBER = 60;

	public static double[] getOneHourData(MetricItem report, MetricType type) {
		double[] result = new double[POINT_NUMBER];
		Map<Integer, Point> map = report.getAbtests().get("-1").getGroups().get("").getPoints();
		for (Integer minute : map.keySet()) {
			if (minute >= 0 && minute < POINT_NUMBER) {
				Point point = map.get(minute);
				if (type == MetricType.AVG) {
					result[minute] = point.getAvg();
				} else if (type == MetricType.COUNT) {
					result[minute] = (double) point.getCount();
				} else if (type == MetricType.SUM) {
					result[minute] = point.getSum();
				}
			}
		}
		return result;
	}
	
	public static double[] getOneDayData(List<MetricItem> reports, MetricType type) {
		double[] values = new double[POINT_NUMBER];
		for (int i = 0; i < POINT_NUMBER; i++) {
			values[i] = -1;
		}
		int hour = 0;
		for (MetricItem report : reports) {
			try {
				double[] oneHourValues = MetricPointParser.getOneHourData(report, type);
				
				for (int minute = 0; minute < 60; minute ++) {
					int index = hour * 60 + minute;
					values[index] = oneHourValues[minute];
				}
			} catch (NullPointerException e) {
				// Do Nothing
			}
			hour++;
		}
		return values;
	}
}
