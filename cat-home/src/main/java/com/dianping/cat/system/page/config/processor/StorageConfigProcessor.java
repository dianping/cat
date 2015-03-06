package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.system.config.StorageCacheRuleConfigManager;
import com.dianping.cat.system.config.StorageDatabaseRuleConfigManager;
import com.dianping.cat.system.config.StorageRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class StorageConfigProcessor extends BaseProcesser {

	@Inject
	private StorageDatabaseRuleConfigManager m_databaseConfigManager;

	@Inject
	private StorageCacheRuleConfigManager m_cacheConfigManager;

	public void process(Action action, Payload payload, Model model) {
		String type = payload.getType();
		StorageRuleConfigManager configManager = null;

		if ("cache".equals(type)) {
			configManager = m_cacheConfigManager;
		} else {
			configManager = m_databaseConfigManager;
		}

		switch (action) {
		case STORAGE_RULE:
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		case STORAGE_RULE_ADD_OR_UPDATE:
			generateRuleConfigContent(payload.getRuleId(), configManager, model);
			break;
		case STORAGE_RULE_ADD_OR_UPDATE_SUBMIT:
			model.setOpState(addSubmitRule(configManager, payload.getRuleId(), "", payload.getConfigs()));
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		case STORAGE_RULE_DELETE:
			model.setOpState(deleteRule(configManager, payload.getRuleId()));
			model.setRules(configManager.getMonitorRules().getRules().values());
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}
}
