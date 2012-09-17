package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;

public interface Rule {

	String getName();
	
	long getRuleId();

	boolean init(RuleEntity entity);

	boolean isEligible(Event event);

	boolean apply(Event event);
	
	void setDispatcher(EventDispatcher dispatcher);

}
