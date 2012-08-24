package com.dianping.dog.alarm.rule;

public interface Rule {
	public String getName();

	public boolean apply(RuleContext ctx);
}
