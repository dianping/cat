package com.dianping.cat.report.page.metric;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class MetricConfig {

	private Map<String, MetricFlag> m_flags = new LinkedHashMap<String, MetricFlag>();

	public MetricFlag get(String key) {
		return m_flags.get(key);
	}

	public Collection<MetricFlag> getFlags() {
		return m_flags.values();
	}

	public void put(MetricFlag flag) {
		m_flags.put(flag.getKey(), flag);
	}

	public static class MetricFlag {
		private String m_key;

		private String m_key2;

		private int m_index;

		private boolean m_showCount;

		private boolean m_showSum;

		private boolean m_showAvg;

		private String m_title;

		public MetricFlag(String key, String key2, int index, boolean showCount, boolean showSum, boolean showAvg,
		      String title) {
			m_key = key;
			m_key2 = key;
			m_index = index;
			m_showCount = showCount;
			m_showSum = showSum;
			m_showAvg = showAvg;
			m_title = title;
		}

		public int getIndex() {
			return m_index;
		}

		public String getKey() {
			return m_key;
		}

		public String getKey2() {
			return m_key2;
		}

		public String getTitle() {
			return m_title;
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

		public void setKey2(String key2) {
			m_key2 = key2;
		}

		public void setTitle(String title) {
			m_title = title;
		}
	}

}
