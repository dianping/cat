package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.system.SystemRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class SystemConfigProcessor extends BaseProcesser {

	@Inject
	private SystemRuleConfigManager m_systemRuleConfigManager;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case SYSTEM_RULE_CONFIG_LIST:
			generateRuleItemList(m_systemRuleConfigManager, model);
			break;
		case SYSTEM_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getKey(), m_systemRuleConfigManager, model);
			break;
		case SYSTEM_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_systemRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
			generateRuleItemList(m_systemRuleConfigManager, model);
			break;
		case SYSTEM_RULE_DELETE:
			model.setOpState(deleteRule(m_systemRuleConfigManager, payload.getKey()));
			generateRuleItemList(m_systemRuleConfigManager, model);
			break;

		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
