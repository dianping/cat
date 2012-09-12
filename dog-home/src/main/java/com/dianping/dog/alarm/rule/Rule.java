package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.alarm.entity.RuleEntity;

public interface Rule {
	
	public boolean init(RuleEntity entity);
	
	public String getName();
	
	public boolean isEligible(DataEvent event);

	public boolean apply(DataEvent event);
			
}
