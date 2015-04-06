package com.dianping.cat.system.page.config.processor;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.storage.StorageCacheRuleConfigManager;
import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;
import com.dianping.cat.report.alert.storage.StorageSQLRuleConfigManager;
import com.dianping.cat.report.page.storage.StorageConstants;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class StorageConfigProcessor extends BaseProcesser {

	@Inject
	private StorageSQLRuleConfigManager m_SQLConfigManager;

	@Inject
	private StorageCacheRuleConfigManager m_cacheConfigManager;

	public void process(Action action, Payload payload, Model model) {
		String type = payload.getType();
		StorageRuleConfigManager configManager = null;

		if (StorageConstants.CACHE_TYPE.equals(type)) {
			configManager = m_cacheConfigManager;
		} else if (StorageConstants.SQL_TYPE.equals(type)) {
			configManager = m_SQLConfigManager;
		} else {
			throw new RuntimeException("Error type: " + type);
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
