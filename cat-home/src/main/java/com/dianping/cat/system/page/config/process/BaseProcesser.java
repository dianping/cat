package com.dianping.cat.system.page.config.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hsqldb.lib.StringUtil;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultJsonBuilder;
import com.dianping.cat.report.task.alert.RuleFTLDecorator;
import com.dianping.cat.system.config.BaseRuleConfigManager;
import com.dianping.cat.system.page.config.Model;

public class BaseProcesser {

	@Inject
	protected RuleFTLDecorator m_ruleDecorator;
	
	public boolean addSubmitRule(BaseRuleConfigManager manager, String id, String metrics, String configs) {
		try {
			String xmlContent = manager.updateRule(id, metrics, configs);

			return manager.insert(xmlContent);
		} catch (Exception ex) {
			return false;
		}
	}
	
	public boolean deleteRule(BaseRuleConfigManager manager, String key) {
		try {
			String xmlContent = manager.deleteRule(key);
			return manager.insert(xmlContent);
		} catch (Exception ex) {
			return false;
		}
	}

	public void generateRuleConfigContent(String key, BaseRuleConfigManager manager, Model model) {
		String configsStr = "";
		String ruleId = "";

		if (!StringUtil.isEmpty(key)) {
			Rule rule = manager.queryRule(key);
			ruleId = rule.getId();
			configsStr = new DefaultJsonBuilder(true).buildArray(rule.getConfigs());

			String configHeader = new DefaultJsonBuilder(true).buildArray(rule.getMetricItems());

			model.setConfigHeader(configHeader);
		}
		String content = m_ruleDecorator.generateConfigsHtml(configsStr);

		model.setContent(content);
		model.setId(ruleId);
	}
	
	public void generateRuleEditContent(String key, String href, String metricsTemplate, String configsTemplate,
	      BaseRuleConfigManager manager, Model model) {
		String metricsStr = "";
		String configsStr = "";
		String ruleId = "";

		if (!StringUtil.isEmpty(key)) {
			Rule rule = manager.queryRule(key);
			ruleId = rule.getId();
			metricsStr = new DefaultJsonBuilder().buildArray(rule.getMetricItems());
			configsStr = new DefaultJsonBuilder().buildArray(rule.getConfigs());
		}
		String metricsContent = m_ruleDecorator.generateMetricItemsHtml(metricsStr, metricsTemplate);
		String configsContent = m_ruleDecorator.generateConfigsHtml(href, configsStr, configsTemplate);

		model.setId(ruleId);
		model.setContent(metricsContent + configsContent);
	}

	public void generateRuleItemList(BaseRuleConfigManager manager, Model model) {
		Collection<Rule> rules = manager.getMonitorRules().getRules().values();
		List<RuleItem> ruleItems = new ArrayList<RuleItem>();

		for (Rule rule : rules) {
			String id = rule.getId();
			List<MetricItem> items = rule.getMetricItems();

			if (items.size() > 0) {
				MetricItem item = items.get(0);
				String productText = item.getProductText();
				String metricText = item.getMetricItemText();
				RuleItem ruleItem = new RuleItem(id, productText, metricText);

				ruleItem.setMonitorCount(item.isMonitorCount());
				ruleItem.setMonitorAvg(item.isMonitorAvg());
				ruleItem.setMonitorSum(item.isMonitorSum());

				ruleItems.add(ruleItem);
			}
		}
		model.setRuleItems(ruleItems);
	}


	public class RuleItem {
		private String m_id;

		private String m_productlineText;

		private String m_metricText;

		private boolean m_monitorCount;

		private boolean m_monitorSum;

		private boolean m_monitorAvg;

		public RuleItem(String id, String productlineText, String metricText) {
			m_id = id;
			m_productlineText = productlineText;
			m_metricText = metricText;
		}

		public String getId() {
			return m_id;
		}

		public String getMetricText() {
			return m_metricText;
		}

		public String getProductlineText() {
			return m_productlineText;
		}

		public boolean isMonitorAvg() {
			return m_monitorAvg;
		}

		public boolean isMonitorCount() {
			return m_monitorCount;
		}

		public boolean isMonitorSum() {
			return m_monitorSum;
		}

		public void setId(String id) {
			m_id = id;
		}

		public void setMetricText(String metricText) {
			m_metricText = metricText;
		}

		public void setMonitorAvg(boolean monitorAvg) {
			m_monitorAvg = monitorAvg;
		}

		public void setMonitorCount(boolean monitorCount) {
			m_monitorCount = monitorCount;
		}

		public void setMonitorSum(boolean monitorSum) {
			m_monitorSum = monitorSum;
		}

		public void setProductlineText(String productlineText) {
			m_productlineText = productlineText;
		}
	}

}
