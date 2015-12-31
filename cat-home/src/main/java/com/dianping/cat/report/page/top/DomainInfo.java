package com.dianping.cat.report.page.top;

import java.util.LinkedHashMap;
import java.util.Map;

public class DomainInfo {

	public Map<String, Metric> m_metrics = new LinkedHashMap<String, Metric>();

	public Metric getMetric(String key) {
		Metric m = m_metrics.get(key);

		if (m == null) {
			m = new Metric();
			m_metrics.put(key, m);
		}
		return m;
	}

	public Map<String, Metric> getMetrics() {
		return m_metrics;
	}

	public static class Item {
		private long m_count;

		private double m_avg;

		private long m_fail;

		private double m_sum;

		public double getAvg() {
			return m_avg;
		}

		public long getCount() {
			return m_count;
		}

		public long getFail() {
			return m_fail;
		}

		public Item setValue(long count, double sum) {
			m_count = m_count + count;
			m_sum = m_sum + sum;

			if (m_count > 0) {
				m_avg = m_sum / m_count;
			}
			return this;
		}

		public Item setFail(long fail) {
			m_fail = fail;
			return this;
		}
	}

	public static class Metric {

		private int m_exception;

		private Map<String, Item> m_items = new LinkedHashMap<String, Item>();

		public void addException(int count) {
			m_exception = m_exception + count;
		}

		public Item get(String key) {
			Item item = m_items.get(key);

			if (item == null) {
				item = new Item();
				m_items.put(key, item);
			}
			return item;
		}

		public int getException() {
			return m_exception;
		}

		public void setException(int exception) {
			m_exception = exception;
		}
	}

}
