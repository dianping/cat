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

	private ConcurrentHashMap<String, Long> m_alertInfos = new ConcurrentHashMap<String, Long>();

	@Inject
	protected MetricConfigManager m_manager;

	public void addAlertInfo(String metricId, long value) {
		m_alertInfos.put(metricId, value);
	}

	@Override
	public void initialize() throws InitializationException {
	}

	public List<String> queryLastestAlarmKey(int minute) {
		List<String> keys = new ArrayList<String>();
		long currentTimeMillis = System.currentTimeMillis();

		for (Entry<String, Long> entry : m_alertInfos.entrySet()) {
			Long value = entry.getValue();

			if (currentTimeMillis - value < TimeUtil.ONE_MINUTE * minute) {
				keys.add(entry.getKey());
			}
		}

		return keys;
	}

}
