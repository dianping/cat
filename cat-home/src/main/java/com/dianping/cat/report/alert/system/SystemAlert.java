package com.dianping.cat.report.alert.system;

import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.BaseAlert;
import com.dianping.cat.report.alert.config.BaseRuleConfigManager;

public class SystemAlert extends BaseAlert {

	@Inject
	protected SystemRuleConfigManager m_ruleConfigManager;

	@Override
	public String getName() {
		return AlertType.System.getName();
	}

	@Override
	protected BaseRuleConfigManager getRuleConfigManager() {
		return m_ruleConfigManager;
	}

	@Override
	protected Map<String, ProductLine> getProductlines() {
		return m_productLineConfigManager.querySystemProductLines();
	}
}