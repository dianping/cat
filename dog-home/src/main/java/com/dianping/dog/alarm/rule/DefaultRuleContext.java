package com.dianping.dog.alarm.rule;

import java.util.HashMap;
import java.util.Map;

public class DefaultRuleContext implements RuleContext {
	private Map<String, Object> m_attributes = new HashMap<String, Object>();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name) {
		return (T) m_attributes.get(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getAttribute(String name, T defaultValue) {
		Object value = m_attributes.get(name);

		if (value == null) {
			return defaultValue;
		} else {
			return (T) value;
		}
	}

	@Override
	public void setAttribute(String name, Object value) {
		m_attributes.put(name, value);
	}
}