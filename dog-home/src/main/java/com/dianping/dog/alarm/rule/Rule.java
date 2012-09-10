package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.event.Event;

public interface Rule {
	
	public boolean init(RuleEntity entity);
	
	public String getName();
	
	public boolean isEligible(Event event);

	public boolean apply(Event event);
			
}
