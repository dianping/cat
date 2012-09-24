package com.dianping.dog.alarm.rule;

import com.dianping.dog.dal.Ruletemplate;
import com.site.lookup.ContainerHolder;

public class RuleLoaderFactory extends ContainerHolder{
	
	public static String PROBLEM_TEMPLATE = "problem";
	
	RuleLoader getRuleLoader(Ruletemplate template){
		if(template.getType().equals(PROBLEM_TEMPLATE)){
			return lookup(ProblemRuleLoader.class);
		}
		return null;
	}

}
