package com.dianping.dog.alarm.rule;

import org.codehaus.plexus.logging.LogEnabled;

import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;

public interface Rule extends LogEnabled {

	String getName();
	
	int getRuleId();

	RuleEntity getRuleEntity();
	
	boolean init(RuleEntity entity);

	boolean isEligible(Event event);

	boolean apply(Event event);
	
	void setDispatcher(EventDispatcher dispatcher);

}
