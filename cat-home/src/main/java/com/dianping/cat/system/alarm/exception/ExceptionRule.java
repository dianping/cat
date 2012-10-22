package com.dianping.cat.system.alarm.exception;

import java.util.HashMap;
import java.util.Map;

public class ExceptionRule {

	private String m_connectUrl;

	private Map<String, Duration> m_durations = new HashMap<String,Duration>();
	
	public String getConnectUrl() {
		return m_connectUrl;
	}

	public void setConnectUrl(String connectUrl) {
		m_connectUrl = connectUrl;
	}

	public Map<String, Duration> getDurations() {
		return m_durations;
	}

	public void setDurations(Map<String, Duration> durations) {
		m_durations = durations;
	}

	public static class Duration {

		private int m_interval;

		private int m_min;

		private int m_max;

		private String m_strategy;

		public int getInterval() {
			return m_interval;
		}

		public void setInterval(int interval) {
			m_interval = interval;
		}

		public int getMin() {
			return m_min;
		}

		public void setMin(int min) {
			m_min = min;
		}

		public int getMax() {
			return m_max;
		}

		public void setMax(int max) {
			m_max = max;
		}

		public String getStrategy() {
			return m_strategy;
		}

		public void setStrategy(String strategy) {
			m_strategy = strategy;
		}
	}

}
