package com.dianping.cat.system.config;

import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.Condition;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Subcondition;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.report.task.alert.MetricType;

public class BusinessRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	private static final String CONFIG_NAME = "businessRulesConfig";

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String metricKey, MetricType type) {
		List<com.dianping.cat.home.rule.entity.Config> configs = super.queryConfigs(metricKey, type);

		if (configs.size() == 0) {
			com.dianping.cat.home.rule.entity.Config config = new com.dianping.cat.home.rule.entity.Config();
			Condition condition = new Condition();
			Subcondition descPerSubcon = new Subcondition();
			Subcondition descValSubcon = new Subcondition();

			descPerSubcon.setType("DescPer").setText("50");
			descValSubcon.setType("DescVal").setText("100");
			condition.addSubcondition(descPerSubcon).addSubcondition(descValSubcon);
			config.addCondition(condition);
			configs.add(config);
		}
		return configs;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_config = DefaultSaxParser.parse(content);
			m_configId = config.getId();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-business-metric-rule-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_config = DefaultSaxParser.parse(content);
				m_configId = config.getId();
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

}
