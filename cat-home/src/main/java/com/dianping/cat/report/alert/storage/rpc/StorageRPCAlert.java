package com.dianping.cat.report.alert.storage.rpc;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.report.alert.storage.AbstractStorageAlert;
import com.dianping.cat.report.alert.storage.StorageRuleConfigManager;

@Named
public class StorageRPCAlert extends AbstractStorageAlert {

	public static final String ID = AlertType.STORAGE_RPC.getName();

	@Inject
	private StorageRPCRuleConfigManager m_configManager;

	@Override
	public String getName() {
		return ID;
	}

	@Override
	protected StorageRuleConfigManager getRuleConfigManager() {
		return m_configManager;
	}
}
