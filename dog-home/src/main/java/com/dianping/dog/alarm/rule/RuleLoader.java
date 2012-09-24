package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.dal.Ruleinstance;
import com.dianping.dog.dal.Ruletemplate;

public interface RuleLoader {
	
	RuleEntity loadRuleEntity(Ruleinstance instance,Ruletemplate template);

}
