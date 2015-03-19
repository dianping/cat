package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.heartbeat.HeartbeatRuleConfigManager;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class HeartbeatConfigProcessor extends BaseProcesser {

	@Inject
	private HeartbeatRuleConfigManager m_heartbeatRuleConfigManager;

	@Inject
	private HeartbeatDisplayPolicyManager m_displayPolicyManager;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case HEARTBEAT_RULE_CONFIG_LIST:
			generateRuleItemList(m_heartbeatRuleConfigManager, model);
			break;
		case HEARTBEAT_RULE_ADD_OR_UPDATE:
			model.setHeartbeatExtensionMetrics(m_displayPolicyManager.queryAlertMetrics());
			generateRuleConfigContent(payload.getKey(), m_heartbeatRuleConfigManager, model);
			break;
		case HEARTBEAT_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_heartbeatRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
			generateRuleItemList(m_heartbeatRuleConfigManager, model);
			break;
		case HEARTBEAT_RULE_DELETE:
			model.setOpState(deleteRule(m_heartbeatRuleConfigManager, payload.getKey()));
			generateRuleItemList(m_heartbeatRuleConfigManager, model);
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
