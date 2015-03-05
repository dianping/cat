package com.dianping.cat.report.alert.storage;

import java.util.Map;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.BaseAlert;
import com.dianping.cat.system.config.BaseRuleConfigManager;

public class StorageAlert extends BaseAlert {

	@Override
	public String getName() {
		return AlertType.Storage.getName();
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		throw new RuntimeException("get productline is not support by storage alert");
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		// TODO Auto-generated method stub
		return null;
	}

}
