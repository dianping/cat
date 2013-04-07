package com.dianping.cat.report.page.metric;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MetricConfig {

	private Map<String, MetricFlag> m_flags = new LinkedHashMap<String, MetricFlag>();

	public void put(MetricFlag flag) {
		m_flags.put(flag.getKey(), flag);
	}

	public MetricFlag get(String key) {
		return m_flags.get(key);
	}
	
	public Collection<MetricFlag> getFlags(){
		return m_flags.values();
	}

	public static class MetricFlag {
		private String m_key;

		private int m_index;

		private boolean m_showCount;

		private boolean m_showSum;

		private boolean m_showAvg;

		public MetricFlag(String key, int index, boolean showCount, boolean showSum, boolean showAvg) {
			m_key = key;
			m_index = index;
			m_showCount = showCount;
			m_showSum = showSum;
			m_showAvg = showAvg;
		}

		public int getIndex() {
			return m_index;
		}

		public String getKey() {
			return m_key;
		}

		public boolean isShowAvg() {
			return m_showAvg;
		}

		public boolean isShowCount() {
			return m_showCount;
		}

		public boolean isShowSum() {
			return m_showSum;
		}

	}

}
