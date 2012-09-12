package com.dianping.dog.alarm.rule;

import com.dianping.dog.event.Event;

public interface Rule {
	
	public String getName();
	
	public boolean isEligible(Event event);

	public boolean apply(Event event);
			
}
