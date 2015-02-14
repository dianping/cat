package com.dianping.cat.report.alert;

import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;

public class MetricReportGroup {

	private MetricReport m_last;

	private MetricReport m_current;

	private State m_type;

	private boolean m_dataReady;

	public double[] extractData(int currentMinute, int ruleMinute, String metricKey, MetricType type) {
		double[] value = new double[ruleMinute];

		if (currentMinute >= ruleMinute - 1) {
			int start = currentMinute + 1 - ruleMinute;
			int end = currentMinute;

			value = queryRealData(start, end, metricKey, m_current, type);
		} else if (currentMinute < 0) {
			int start = 60 + currentMinute + 1 - (ruleMinute);
			int end = 60 + currentMinute;

			value = queryRealData(start, end, metricKey, m_last, type);
		} else {
			int currentStart = 0, currentEnd = currentMinute;
			double[] currentValue = queryRealData(currentStart, currentEnd, metricKey, m_current, type);

			int lastStart = 60 + 1 - (ruleMinute - currentMinute);
			int lastEnd = 59;
			double[] lastValue = queryRealData(lastStart, lastEnd, metricKey, m_last, type);

			value = mergerArray(lastValue, currentValue);
		}

		return value;
	}

	public MetricReport getCurrent() {
		return m_current;
	}

	public MetricReport getLast() {
		return m_last;
	}

	public Map<String, MetricItem> getMetricItem() {
		if (m_type == State.LAST) {
			return m_last.getMetricItems();
		} else {
			return m_current.getMetricItems();
		}
	}

	public State getType() {
		return m_type;
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

	private double[] queryRealData(int start, int end, String metricKey, MetricReport report, MetricType type) {
		double[] all = new double[60];
		Map<Integer, Segment> map = report.findOrCreateMetricItem(metricKey).getSegments();

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
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(all, start, result, 0, length);

		return result;
	}

	public MetricReportGroup setCurrent(MetricReport current) {
		m_current = current;
		return this;
	}

	public void setDataReady(boolean dataReady) {
		m_dataReady = dataReady;
	}

	public MetricReportGroup setLast(MetricReport last) {
		m_last = last;
		return this;
	}

	public MetricReportGroup setType(State type) {
		m_type = type;
		return this;
	}

	public static enum State {
		CURRENT(1),

		LAST(2),

		CURRENT_LAST(3);

		private int m_value;

		private State(int value) {
			m_value = value;
		}

		public int getValue() {
			return m_value;
		}

		public void setValue(int value) {
			m_value = value;
		}
	}

}
