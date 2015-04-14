package com.dianping.cat.report.alert.database;

import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.BaseAlert;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;

public class DatabaseAlert extends BaseAlert {

	@Inject
	protected DatabaseRuleConfigManager m_ruleConfigManager;

	@Override
	public String getName() {
		return AlertType.DataBase.getName();
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		return m_productLineConfigManager.queryDatabaseProductLines();
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}
}