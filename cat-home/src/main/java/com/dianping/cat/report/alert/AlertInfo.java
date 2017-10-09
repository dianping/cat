package com.dianping.cat.report.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.helper.TimeHelper;

public class AlertInfo implements Initializable {

	private ConcurrentHashMap<AlertMetric, Long> m_alertInfos = new ConcurrentHashMap<AlertMetric, Long>();

	@Inject
	protected MetricConfigManager m_manager;

	public void addAlertInfo(String group, String metricId, long value) {
		m_alertInfos.put(new AlertMetric(group, metricId), value);
	}

	@Override
	public void initialize() throws InitializationException {
	}

	public List<AlertMetric> queryLastestAlarmKey(int minute) {
		List<AlertMetric> keys = new ArrayList<AlertMetric>();
		long currentTimeMillis = System.currentTimeMillis();

		for (Entry<AlertMetric, Long> entry : m_alertInfos.entrySet()) {
			Long value = entry.getValue();

			if (currentTimeMillis - value < TimeHelper.ONE_MINUTE * minute) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

	public class AlertMetric {

		private String m_group;

		private String m_metricId;

		public AlertMetric(String group, String metricId) {
			m_group = group;
			m_metricId = metricId;
		}

		@Override
		public boolean equals(Object obj) {
			AlertMetric other = (AlertMetric) obj;

			if (m_group.equals(other.getGroup()) && m_metricId.equals(other.getMetricId())) {
				return true;
			} else {
				return false;
			}
		}

		public String getGroup() {
			return m_group;
		}

		public String getMetricId() {
			return m_metricId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((m_group == null) ? 0 : m_group.hashCode());
			result = prime * result + ((m_metricId == null) ? 0 : m_metricId.hashCode());
			return result;
		}
	}

}
