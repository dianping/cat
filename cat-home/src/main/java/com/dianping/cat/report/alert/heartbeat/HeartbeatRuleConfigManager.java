package com.dianping.cat.report.alert.heartbeat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;

public class HeartbeatRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	@Inject
	private ContentFetcher m_fetcher;

	private static final String CONFIG_NAME = "heartbeatRuleConfig";

	private void addRuleToMap(Rule rule, String metric, int priority, Map<String, Map<Integer, List<Rule>>> rules) {
		Map<Integer, List<Rule>> rulesByPriority = rules.get(metric);

		if (rulesByPriority == null) {
			rulesByPriority = new HashMap<Integer, List<Rule>>();

			rules.put(metric, rulesByPriority);
		}

		List<Rule> ruleList = rulesByPriority.get(priority);

		if (ruleList == null) {
			ruleList = new ArrayList<Rule>();

			rulesByPriority.put(priority, ruleList);
		}

		ruleList.add(rule);
	}

	private Map<String, List<com.dianping.cat.home.rule.entity.Config>> extractConfigs(String domain,
	      Map<String, Map<Integer, List<Rule>>> rulesByMetricPriority) {
		Map<String, List<com.dianping.cat.home.rule.entity.Config>> result = new HashMap<String, List<com.dianping.cat.home.rule.entity.Config>>();

		for (Entry<String, Map<Integer, List<Rule>>> entry : rulesByMetricPriority.entrySet()) {
			String metric = entry.getKey();
			List<Rule> rules = getMaxPriorityRules(entry.getValue());
			List<com.dianping.cat.home.rule.entity.Config> configs = new ArrayList<com.dianping.cat.home.rule.entity.Config>();

			for (Rule rule : rules) {
				configs.addAll(rule.getConfigs());

				String nameValuePairs = "product=" + domain + "&metricKey=" + metric;
				Cat.logEvent("FindRule:" + getConfigName(), rule.getId(), Event.SUCCESS, nameValuePairs);
			}
			result.put(metric, configs);
		}
		return result;
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

	public Map<String, List<com.dianping.cat.home.rule.entity.Config>> queryConfigsByDomain(String domain) {
		Map<String, Map<Integer, List<Rule>>> rules = new HashMap<String, Map<Integer, List<Rule>>>();

		for (Rule rule : m_config.getRules().values()) {
			for (MetricItem metricItem : rule.getMetricItems()) {
				String domainPattern = metricItem.getProductText();
				int matchLevel = validateRegex(domainPattern, domain);

				if (matchLevel > 0) {
					String metric = metricItem.getMetricItemText();

					addRuleToMap(rule, metric, matchLevel, rules);
				}
			}
		}

		return extractConfigs(domain, rules);
	}

}
