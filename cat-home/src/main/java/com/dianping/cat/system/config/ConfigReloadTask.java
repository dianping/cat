package com.dianping.cat.system.config;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.core.ProductLineConfigManager;

public class ConfigReloadTask implements Initializable {

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Override
	public void initialize() throws InitializationException {
		Threads.forGroup("Cat").start(new Reload());
	}

	public class Reload implements Task {
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

}
