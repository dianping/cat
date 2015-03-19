package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.database.DatabaseRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class DatabaseConfigProcessor extends BaseProcesser {

	@Inject
	private DatabaseRuleConfigManager m_databaseRuleConfigManager;

	public void process(Action action, Payload payload, Model model) {
		switch (action) {

		case DATABASE_RULE_CONFIG_LIST:
			generateRuleItemList(m_databaseRuleConfigManager, model);
			break;
		case DATABASE_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getKey(), m_databaseRuleConfigManager, model);
			break;
		case DATABASE_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(m_databaseRuleConfigManager, payload.getRuleId(), payload.getMetrics(),
			      payload.getConfigs()));
			generateRuleItemList(m_databaseRuleConfigManager, model);
			break;
		case DATABASE_RULE_DELETE:
			model.setOpState(deleteRule(m_databaseRuleConfigManager, payload.getKey()));
			generateRuleItemList(m_databaseRuleConfigManager, model);
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
