package com.dianping.dog.alarm.rule;

import java.util.List;

public interface RuleManager {
	
   boolean addRule(Rule rule);
   
   boolean removeRule(Rule rule);
   
   List<Rule> getRules();

}
