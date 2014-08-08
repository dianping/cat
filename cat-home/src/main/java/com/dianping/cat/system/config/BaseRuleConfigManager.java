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
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.MetricType;
import com.site.lookup.util.StringUtils;

public abstract class BaseRuleConfigManager {

	@Inject
	protected ConfigDao m_configDao;

	protected int m_configId;

	protected MonitorRules m_config;

	protected abstract String getCategoryName();

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

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String product, String metricKey, MetricType type) {
		List<com.dianping.cat.home.rule.entity.Config> configs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> items = rule.getMetricItems();

			for (MetricItem item : items) {
				String productText = item.getProductText();
				String metricItemText = item.getMetricItemText();
				boolean validate = false;

				if (type == MetricType.COUNT && item.isMonitorCount()) {
					validate = validate(productText, metricItemText, product, metricKey);
				} else if (type == MetricType.AVG && item.isMonitorAvg()) {
					validate = validate(productText, metricItemText, product, metricKey);
				} else if (type == MetricType.SUM && item.isMonitorSum()) {
					validate = validate(productText, metricItemText, product, metricKey);
				}

				if (validate) {
					configs.addAll(rule.getConfigs());
					Cat.logEvent("FindRule:" + getCategoryName(), rule.getId(), Event.SUCCESS, product + "," + metricKey);
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

	public boolean validate(String productText, String metricKeyText, String product, String metricKey) {
		if (StringUtils.isEmpty(productText)) {
			return validateRegex(metricKeyText, metricKey);
		} else {
			if (validateRegex(productText, product)) {
				return validateRegex(metricKeyText, metricKey);
			} else {
				return false;
			}
		}
	}

	public boolean validateRegex(String regexText, String text) {
		Pattern p = Pattern.compile(regexText);
		Matcher m = p.matcher(text);

		if (m.find()) {
			return true;
		} else {
			return false;
		}
	}

}
