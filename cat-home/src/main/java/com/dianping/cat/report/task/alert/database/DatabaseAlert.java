package com.dianping.cat.report.task.alert.database;

import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.BaseAlert;
import com.dianping.cat.system.config.BaseRuleConfigManager;
import com.dianping.cat.system.config.DatabaseRuleConfigManager;

public class DatabaseAlert extends BaseAlert {

	@Inject
	protected DatabaseRuleConfigManager m_ruleConfigManager;

	@Override
	public String getName() {
		return AlertType.DataBase.getName();
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		return m_productLineConfigManager.queryDatabases();
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}
}