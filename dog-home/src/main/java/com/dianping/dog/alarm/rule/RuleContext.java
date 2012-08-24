package com.dianping.dog.alarm.rule;

public interface RuleContext {
	public <T> T getAttribute(String name);

	public <T> T getAttribute(String name, T defaultValue);

	public void setAttribute(String name, Object value);
}
