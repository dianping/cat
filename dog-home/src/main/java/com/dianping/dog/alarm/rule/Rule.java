package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.data.DataEvent;

public interface Rule {
	
	public String getName();
	
	public boolean isEligible(DataEvent event);

	public boolean apply(DataEvent event);
			
}
