package com.dianping.cat.system.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.entity.SubCondition;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.MetricType;

public class BusinessRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	private static final String CATEGORY_NAME = "business";

	private static final String CONFIG_NAME = "businessRuleConfig";

	public String updateRule(String ruleContent) throws SAXException, IOException {
		Rule rule = DefaultSaxParser.parseEntity(Rule.class, ruleContent);
		String metricKey = rule.getId();

		m_config.getRules().put(metricKey, rule);
		return m_config.toString();
	}

	private com.dianping.cat.home.rule.entity.Config buildDefaultConfig() {
		com.dianping.cat.home.rule.entity.Config config = new com.dianping.cat.home.rule.entity.Config();
		Condition condition = new Condition();
		SubCondition descPerSubcon = new SubCondition();
		SubCondition descValSubcon = new SubCondition();

		descPerSubcon.setType("DescPer").setText("50");
		descValSubcon.setType("DescVal").setText("100");
		condition.addSubCondition(descPerSubcon).addSubCondition(descValSubcon);
		config.addCondition(condition);

		return config;
	}

	private Rule buildDefaultRule(String product, String metricKey) {
		Rule rule = new Rule(metricKey);
		MetricItem item = new MetricItem();

		item.setProductText(product);
		item.setMetricItemText(metricKey);

		rule.addMetricItem(item);
		rule.addConfig(buildDefaultConfig());
		return rule;
	}

	@Override
	protected String getCategoryName() {
		return CATEGORY_NAME;
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
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-business-metric-rule-config.xml"), "utf-8");
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

	@Override
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
				}
			}

			if (configs.size() == 0) {
				configs.add(buildDefaultConfig());
			}

			Cat.logEvent("FindRule:" + getCategoryName(), rule.getId(), Event.SUCCESS, product + "," + metricKey);
			return configs;
		}
	}

	public Rule queryRule(String product, String metricKey) {
		Rule rule = m_config.getRules().get(metricKey);

		if (rule != null) {
			return rule;
		} else {
			return buildDefaultRule(product, metricKey);
		}
	}

}
