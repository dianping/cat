package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.report.task.alert.MetricType;

public abstract class BaseRuleConfigManager {

	@Inject
	protected ConfigDao m_configDao;

	protected int m_configId;

	protected MonitorRules m_config;

	protected abstract String getConfigName();

	public MonitorRules getMonitorRules() {
		return m_config;
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

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String metricKey, MetricType type) {
		List<com.dianping.cat.home.rule.entity.Config> configs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> items = rule.getMetricItems();

			for (MetricItem item : items) {
				String checkType = item.getType();
				String context = item.getText();
				boolean validate = false;

				if (type == MetricType.COUNT && item.isMonitorCount()) {
					validate = validate(checkType, context, metricKey);
				} else if (type == MetricType.AVG && item.isMonitorAvg()) {
					validate = validate(checkType, context, metricKey);
				} else if (type == MetricType.SUM && item.isMonitorSum()) {
					validate = validate(checkType, context, metricKey);
				}

				if (validate) {
					configs.addAll(rule.getConfigs());
					break;
				}
			}
		}
		return configs;
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

	public boolean validate(String type, String con, String key) {
		String context = con.trim();
		String metricKey = key.trim();
		if (type.equals("id")) {
			if (context.equals(metricKey)) {
				return true;
			} else {
				return false;
			}
		} else if (type.equals("regex")) {
			Pattern p = Pattern.compile(context);
			Matcher matcher = p.matcher(metricKey);

			if (matcher.find()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

}
