package com.dianping.cat.report.alert.storage.sql;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;

@Named
public class StorageSQLRuleConfigManager extends StorageRuleConfigManager {

	private static final String CONFIG_NAME = "storageSQLRule";

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

}
