package com.dianping.cat.report.alert.business;

import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.business.model.entity.BusinessItem;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.helper.MetricType;

public class BusinessReportGroup {

	private BusinessReport m_last;

	private BusinessReport m_current;

	private boolean m_dataReady;

	public double[] extractData(int currentMinute, int ruleMinute, String key, MetricType type) {
		double[] value = new double[ruleMinute];

		if (currentMinute >= ruleMinute - 1) {
			int start = currentMinute + 1 - ruleMinute;
			int end = currentMinute;

			value = queryRealData(start, end, key, m_current, type);
		} else if (currentMinute < 0) {
			int start = 60 + currentMinute + 1 - (ruleMinute);
			int end = 60 + currentMinute;

			value = queryRealData(start, end, key, m_last, type);
		} else {
			int currentStart = 0, currentEnd = currentMinute;
			double[] currentValue = queryRealData(currentStart, currentEnd, key, m_current, type);

			int lastStart = 60 + 1 - (ruleMinute - currentMinute);
			int lastEnd = 59;
			double[] lastValue = queryRealData(lastStart, lastEnd, key, m_last, type);

			value = mergerArray(lastValue, currentValue);
		}

		return value;
	}

	public BusinessReport getCurrent() {
		return m_current;
	}

	public BusinessReport getLast() {
		return m_last;
	}

	public boolean isDataReady() {
		return m_dataReady;
	}

	public double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	private double[] queryRealData(int start, int end, String key, BusinessReport report, MetricType type) {
		double[] all = new double[60];
		BusinessItem businessItems = report.findBusinessItem(key);

		if (businessItems != null) {
			Map<Integer, Segment> map = businessItems.getSegments();

			for (Entry<Integer, Segment> entry : map.entrySet()) {
				Integer minute = entry.getKey();
				Segment seg = entry.getValue();

				if (type == MetricType.AVG) {
					all[minute] = seg.getAvg();
				} else if (type == MetricType.COUNT) {
					all[minute] = (double) seg.getCount();
				} else if (type == MetricType.SUM) {
					all[minute] = seg.getSum();
				}
			}
		}
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(all, start, result, 0, length);

		return result;
	}

	public BusinessReportGroup setCurrent(BusinessReport current) {
		m_current = current;
		return this;
	}

	public BusinessReportGroup setDataReady(boolean dataReady) {
		m_dataReady = dataReady;
		return this;
	}

	public BusinessReportGroup setLast(BusinessReport last) {
		m_last = last;
		return this;
	}

}
