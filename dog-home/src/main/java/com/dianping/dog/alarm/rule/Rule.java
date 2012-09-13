package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.event.EventDispatcher;

public interface Rule {

	String getName();

	boolean init(RuleEntity entity);

	boolean isEligible(DataEvent event);

	boolean apply(DataEvent event);
	
	void setDispatcher(EventDispatcher dispatcher);

}
