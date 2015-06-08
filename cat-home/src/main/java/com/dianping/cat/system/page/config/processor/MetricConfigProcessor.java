package com.dianping.cat.system.page.config.processor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.util.StringUtils;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfigManager;
import com.dianping.cat.consumer.metric.MetricConfigManager;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.ConfigHtmlParser;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class MetricConfigProcessor extends BaseProcesser {

	@Inject
	private GlobalConfigProcessor m_globalConfigManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManger;

	@Inject
	private MetricConfigManager m_metricConfigManager;

	@Inject
	private BusinessRuleConfigManager m_businessRuleConfigManager;

	@Inject
	private ConfigHtmlParser m_configHtmlParser;

	private void metricConfigAdd(Payload payload, Model model) {
		String key = m_metricConfigManager.buildMetricKey(payload.getDomain(), payload.getType(), payload.getMetricKey());

		model.setMetricItemConfig(m_metricConfigManager.queryMetricItemConfig(key));
	}

	private boolean metricConfigAddSubmit(Payload payload, Model model) {
		MetricItemConfig config = payload.getMetricItemConfig();
		String domain = config.getDomain();
		String type = config.getType();
		String metricKey = config.getMetricKey();

		if (!StringUtils.isEmpty(domain) && !StringUtils.isEmpty(type) && !StringUtils.isEmpty(metricKey)) {
			config.setId(m_metricConfigManager.buildMetricKey(domain, type, metricKey));

			return m_metricConfigManager.insertMetricItemConfig(config);
		} else {
			return false;
		}
	}

	private void metricConfigList(Payload payload, Model model) {
		Map<String, ProductLine> productLines = m_productLineConfigManger.queryMetricProductLines();
		Map<ProductLine, List<MetricItemConfig>> metricConfigs = new LinkedHashMap<ProductLine, List<MetricItemConfig>>();
		Set<String> exists = new HashSet<String>();
		Set<String> knowDomains = new HashSet<String>();

		for (Entry<String, ProductLine> entry : productLines.entrySet()) {
			ProductLine productLine = entry.getValue();

			Set<String> domains = productLine.getDomains().keySet();
			List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(domains);

			for (MetricItemConfig config : configs) {
				exists.add(m_metricConfigManager.buildMetricKey(config.getDomain(), config.getType(), config.getMetricKey()));
			}
			metricConfigs.put(productLine, configs);
			knowDomains.addAll(domains);

		}

		for (Entry<String, ProductLine> entry : productLines.entrySet()) {
			payload.setProductLineName(entry.getKey());
			break;
		}

		List<MetricItemConfig> otherConfigs = new ArrayList<MetricItemConfig>();

		for (MetricItemConfig config : m_metricConfigManager.getMetricConfig().getMetricItemConfigs().values()) {
			String domain = config.getDomain();

			if (!knowDomains.contains(domain)) {
				otherConfigs.add(config);
			}
		}

		if (!otherConfigs.isEmpty()) {
			metricConfigs.put(new ProductLine("Other").setTitle("Other"), otherConfigs);
		}

		model.setProductMetricConfigs(metricConfigs);
	}

	private void metricRuleAdd(Payload payload, Model model) {
		String ruleId = "";
		String configHeader = "";
		String configsStr = "";
		String key = m_metricConfigManager.buildMetricKey(payload.getDomain(), payload.getType(), payload.getMetricKey());
		Rule rule = m_businessRuleConfigManager.queryRule(payload.getProductLineName(), key);

		if (rule != null) {
			ruleId = rule.getId();
			configHeader = new DefaultJsonBuilder(true).buildArray(rule.getMetricItems());
			configsStr = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());
		}
		String content = m_ruleDecorator.generateConfigsHtml(configsStr);

		model.setId(ruleId);
		model.setConfigHeader(configHeader);
		model.setContent(content);
	}

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case METRIC_CONFIG_ADD_OR_UPDATE:
			metricConfigAdd(payload, model);
			model.setProductLines(m_productLineConfigManger.queryAllProductLines());

			ProductLine productLine = m_productLineConfigManger.queryAllProductLines().get(payload.getProductLineName());
			if (productLine != null) {
				model.setProductLineToDomains(productLine.getDomains());
			}
			model.setProjects(m_globalConfigManager.queryAllProjects());

			List<String> tags = m_metricConfigManager.queryTags();
			model.setTags(tags);
			break;
		case METRIC_CONFIG_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(metricConfigAddSubmit(payload, model));
			metricConfigList(payload, model);
			break;
		case METRIC_RULE_ADD_OR_UPDATE:
			metricRuleAdd(payload, model);
			break;
		case METRIC_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_businessRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
			metricConfigList(payload, model);
			break;
		case METRIC_CONFIG_LIST:
			metricConfigList(payload, model);
			break;
		case METRIC_CONFIG_DELETE:
			model.setOpState(m_metricConfigManager.deleteDomainConfig(m_metricConfigManager.buildMetricKey(
			      payload.getDomain(), payload.getType(), payload.getMetricKey())));
			metricConfigList(payload, model);
			break;
		case METRIC_RULE_CONFIG_UPDATE:
			String domainMetricRuleConfig = payload.getContent();
			if (!StringUtils.isEmpty(domainMetricRuleConfig)) {
				model.setOpState(m_businessRuleConfigManager.insert(domainMetricRuleConfig));
			} else {
				model.setOpState(true);
			}
			model.setContent(m_configHtmlParser.parse(m_businessRuleConfigManager.getMonitorRules().toString()));
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

}
