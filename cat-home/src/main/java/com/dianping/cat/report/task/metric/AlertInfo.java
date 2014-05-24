package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.helper.TimeUtil;

public class AlertInfo implements Initializable {

	private ConcurrentHashMap<MetricItemConfig, Long> m_alerts = new ConcurrentHashMap<MetricItemConfig, Long>();

	@Inject
	protected MetricConfigManager m_manager;

	public void addAlertInfo(MetricItemConfig config, long value) {
		m_alerts.putIfAbsent(config, value);
	}

	public List<MetricItemConfig> queryLastestAlarmInfo(int minute) {
		List<MetricItemConfig> config = new ArrayList<MetricItemConfig>();
		long currentTimeMillis = System.currentTimeMillis();

		for (Entry<MetricItemConfig, Long> entry : m_alerts.entrySet()) {
			Long value = entry.getValue();

			if (currentTimeMillis - value < TimeUtil.ONE_MINUTE * minute) {
				config.add(entry.getKey());
			}
		}

		return config;
	}

	@Override
	public void initialize() throws InitializationException {
	}
}
