package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.problem.ProblemEvent;

public class RuleContext {
	
	ProblemEvent m_event;
	
	public RuleContext(ProblemEvent event){
		this.m_event = event;
	}
   
}
