package com.dianping.cat.report.alert.storage.rpc;

import org.unidal.lookup.annotation.Named;

import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;

@Named
public class StorageRPCRuleConfigManager extends StorageRuleConfigManager {

	private static final String CONFIG_NAME = "storageRPCRule";

	@Override
	protected String getConfigName() {
		return CONFIG_NAME;
	}

}
