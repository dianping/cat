package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.monitorrules.entity.MetricItem;
import com.dianping.cat.home.monitorrules.entity.MonitorRules;
import com.dianping.cat.home.monitorrules.entity.Rule;
import com.dianping.cat.home.monitorrules.transform.DefaultSaxParser;
import com.dianping.cat.report.task.alert.MetricType;

public abstract class BaseMetricRuleConfigManager {

	@Inject
	protected ConfigDao m_configDao;

	protected int m_configId;

	protected MonitorRules m_config;

	private void addConfigsToList(Map<MetricType, List<com.dianping.cat.home.monitorrules.entity.Config>> configsMap,
	      MetricType type, List<com.dianping.cat.home.monitorrules.entity.Config> configs) {
		List<com.dianping.cat.home.monitorrules.entity.Config> configsByMetricType = configsMap.get(type);

		if (configsByMetricType == null) {
			configsByMetricType = new ArrayList<com.dianping.cat.home.monitorrules.entity.Config>();
			configsMap.put(type, configsByMetricType);
		}

		configsByMetricType.addAll(configs);
	}

	private void addConfigsToMap(Map<MetricType, List<com.dianping.cat.home.monitorrules.entity.Config>> configsMap,
	      boolean[] hasMetricTypes, List<com.dianping.cat.home.monitorrules.entity.Config> configs) {
		if (hasMetricTypes[0]) {
			addConfigsToList(configsMap, MetricType.AVG, configs);
		}

		if (hasMetricTypes[1]) {
			addConfigsToList(configsMap, MetricType.COUNT, configs);
		}

		if (hasMetricTypes[2]) {
			addConfigsToList(configsMap, MetricType.SUM, configs);
		}
	}

	protected abstract String getConfigName();

	public MonitorRules getMonitorRules() {
		return m_config;
	}

	public Map<MetricType, List<com.dianping.cat.home.monitorrules.entity.Config>> queryConfigs(String product,
	      String metricKey) {
		Map<MetricType, List<com.dianping.cat.home.monitorrules.entity.Config>> configsMap = new HashMap<MetricType, List<com.dianping.cat.home.monitorrules.entity.Config>>();

		for (Rule rule : m_config.getRules()) {
			boolean[] hasMetricTypes = new boolean[3];

			for (MetricItem metricItem : rule.getMetricItems()) {
				String type = metricItem.getType();

				if (type == null) {
					continue;
				} else if (type.equals("id")) {
					String context = metricItem.getText();

					if (context != null && context.equals(metricKey)) {
						recordMetricTypes(hasMetricTypes, metricItem);
					}
				} else if (type.equals("regex")) {
					String[] context = metricItem.getText().trim().split("\\(s\\)");
					String[] metricInfo = metricKey.split(":");

					Pattern p = Pattern.compile(context[0]);
					Matcher m = p.matcher(product);
					if (!m.find()) {
						continue;
					}

					p = Pattern.compile(context[1]);
					m = p.matcher(metricInfo[0]);
					if (!m.find()) {
						continue;
					}

					p = Pattern.compile(context[2]);
					m = p.matcher(metricInfo[2]);
					if (m.find()) {
						recordMetricTypes(hasMetricTypes, metricItem);
					}
				}
			}

			addConfigsToMap(configsMap, hasMetricTypes, rule.getConfigs());
		}

		return configsMap;
	}

	private void recordMetricTypes(boolean[] hasMetricTypes, MetricItem metricItem) {
		if (metricItem.isMonitorAvg()) {
			hasMetricTypes[0] = true;
		}

		if (metricItem.isMonitorCount()) {
			hasMetricTypes[1] = true;
		}

		if (metricItem.isMonitorSum()) {
			hasMetricTypes[2] = true;
		}
	}
	
	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	protected boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(getConfigName());
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
