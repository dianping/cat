package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.event.EventRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class EventConfigProcessor extends BaseProcesser {

	@Inject
	private EventRuleConfigManager m_configManager;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case EVENT_RULE:
			model.setRules(m_configManager.getMonitorRules().getRules().values());
			break;
		case EVENT_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getRuleId(), m_configManager, model);
			break;
		case EVENT_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_configManager, payload.getRuleId(), "", payload.getConfigs()));
			model.setRules(m_configManager.getMonitorRules().getRules().values());
			break;
		case EVENT_RULE_DELETE:
			model.setOpState(deleteRule(m_configManager, payload.getRuleId()));
			model.setRules(m_configManager.getMonitorRules().getRules().values());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

}
