package com.dianping.dog.alarm.rule;

import java.util.List;

import com.dianping.dog.alarm.entity.RuleEntity;

public interface RuleManager {

	List<Rule> getRules();
	
	Rule getRuleById(long id);
	
	boolean addRule(RuleEntity entity);

}
