package com.dianping.cat.report.alert.storage;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.report.alert.AlertType;

public class StorageCacheAlert extends AbstractStorageAlert {

	@Inject
	private StorageCacheRuleConfigManager m_configManager;

	public static final String ID = AlertType.STORAGE_CACHE.getName();

	@Override
	public String getName() {
		return ID;
	}

	@Override
	protected StorageRuleConfigManager getRuleConfigManager() {
		return m_configManager;
	}
}
