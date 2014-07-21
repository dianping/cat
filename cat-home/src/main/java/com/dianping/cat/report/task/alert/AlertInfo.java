package com.dianping.cat.report.task.alert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.helper.TimeUtil;

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

			if (currentTimeMillis - value < TimeUtil.ONE_MINUTE * minute) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

	public class AlertMetric {

		private String m_group;

		private String m_metricId;

		public AlertMetric(String group, String metricId) {
			this.m_group = group;
			this.m_metricId = metricId;
		}

		public String getGroup() {
			return m_group;
		}

		public String getMetricId() {
			return m_metricId;
		}

	}

}
