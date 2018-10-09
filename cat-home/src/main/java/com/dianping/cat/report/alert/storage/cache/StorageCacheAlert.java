package com.dianping.cat.report.alert.storage.cache;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.report.alert.storage.AbstractStorageAlert;
import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;

@Named
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
