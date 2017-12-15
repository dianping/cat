package com.dianping.cat.report.alert.business;

import java.util.ArrayList;
import java.util.List;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.entity.SubCondition;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.alert.MetricType;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;

public class BusinessRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	@Inject
	protected MetricConfigManager m_metricConfigManager;

	@Inject
	private ContentFetcher m_fetcher;

	private static final String CONFIG_NAME = "businessRuleConfig";

	private com.dianping.cat.home.rule.entity.Config buildDefaultConfig() {
		com.dianping.cat.home.rule.entity.Config config = new com.dianping.cat.home.rule.entity.Config();
		config.setStarttime("00:00");
		config.setEndtime("24:00");

		Condition condition = new Condition();
		SubCondition descPerSubcon = new SubCondition();
		SubCondition descValSubcon = new SubCondition();
		SubCondition flucPerSubcon = new SubCondition();

		descPerSubcon.setType("DescPer").setText("50");
		descValSubcon.setType("DescVal").setText("100");
		flucPerSubcon.setType("FluDescPer").setText("20");
		condition.addSubCondition(descPerSubcon).addSubCondition(descValSubcon).addSubCondition(flucPerSubcon);
		config.addCondition(condition);

		return config;
	}

	private Rule buildDefaultRule(String product, String metricKey) {
		Rule rule = new Rule(metricKey);
		MetricItem item = new MetricItem();

		item.setProductText(product);
		item.setMetricItemText(metricKey);

		MetricItemConfig metricItem = m_metricConfigManager.queryMetricItemConfig(metricKey);
		if (metricItem != null) {
			if (metricItem.isShowAvg()) {
				item.setMonitorAvg(true);
			}
			if (metricItem.isShowCount()) {
				item.setMonitorCount(true);
			}
			if (metricItem.isShowSum()) {
				item.setMonitorSum(true);
			}
		}

		rule.addMetricItem(item);
		rule.addConfig(buildDefaultConfig());
		return rule;
	}

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new MonitorRules();
		}
	}

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String product, String metricKey, MetricType type) {
		Rule rule = m_config.getRules().get(metricKey);
		List<com.dianping.cat.home.rule.entity.Config> configs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

		if (rule == null) {
			configs.add(buildDefaultConfig());
			return configs;
		} else {
			for (MetricItem item : rule.getMetricItems()) {
				if (type == MetricType.COUNT && item.isMonitorCount()) {
					configs.addAll(rule.getConfigs());
					break;
				} else if (type == MetricType.AVG && item.isMonitorAvg()) {
					configs.addAll(rule.getConfigs());
					break;
				} else if (type == MetricType.SUM && item.isMonitorSum()) {
					configs.addAll(rule.getConfigs());
					break;
				} else {
					Cat.logError("No Metric Type find. product:" + product + " metric key:" + metricKey,
					      new RuntimeException());
				}
			}
			if (configs.size() == 0) {
				configs.add(buildDefaultConfig());
			} else {
				Cat.logEvent("FindRule:" + getConfigName(), rule.getId(), Event.SUCCESS, product + "," + metricKey);
			}
			return decorateConfigOnRead(configs);
		}
	}

	public Rule queryRule(String product, String metricKey) {
		Rule rule = m_config.getRules().get(metricKey);

		if (rule != null) {
			return copyRule(rule);
		} else {
			return buildDefaultRule(product, metricKey);
		}
	}

}
