package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.alert.MetricType;

public class HeartbeatRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	@Inject
	private ContentFetcher m_getter;

	private static final String CONFIG_NAME = "heartbeatRuleConfig";

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
				String content = m_getter.getConfigContent(CONFIG_NAME);
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
	public List<com.dianping.cat.home.rule.entity.Config> queryConfigs(String groupText, String metricText,
	      MetricType type) {
		Map<Integer, List<com.dianping.cat.home.rule.entity.Config>> configs = new HashMap<Integer, List<com.dianping.cat.home.rule.entity.Config>>();

		for (Rule rule : m_config.getRules().values()) {
			List<MetricItem> items = rule.getMetricItems();

			for (MetricItem item : items) {
				String productText = item.getProductText();
				String metricItemText = item.getMetricItemText();
				int matchLevel = 0;
				matchLevel = validate(productText, metricItemText, groupText, metricText);

				if (matchLevel > 0) {
					List<com.dianping.cat.home.rule.entity.Config> configList = configs.get(matchLevel);

					if (configList == null) {
						configList = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

						configs.put(matchLevel, configList);
					}
					configList.addAll(rule.getConfigs());
					Cat.logEvent("FindRule:" + getConfigName(), rule.getId(), Event.SUCCESS, productText);
					break;
				}
			}
		}

		List<com.dianping.cat.home.rule.entity.Config> finalConfigs = getMaxPriorityConfigs(configs);

		return decorateConfigOnRead(finalConfigs);
	}
}
