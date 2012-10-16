package com.dianping.cat.system.alarm.exception;

import java.util.List;

import com.dianping.cat.system.alarm.template.ThresholdRule;

public class ExceptionRuleManager {

	public List<ThresholdRule> m_rules;

	public List<ThresholdRule> getAllExceptionRules() {
		return m_rules;
	}
}
