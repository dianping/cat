package com.dianping.cat.system.config;

import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.advanced.ProductLineConfigManager;

public class ConfigReloadTask implements Task {

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricConfigManager m_metricConfigManager;
	
	@Override
	public String getName() {
		return "Config-Reload";
	}

	@Override
	public void run() {
		boolean active = true;
		while (active) {
			try {
				m_productLineConfigManager.refreshProductLineConfig();
				m_metricConfigManager.refreshMetricConfig();
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				Thread.sleep(60 * 1000L);
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

}
