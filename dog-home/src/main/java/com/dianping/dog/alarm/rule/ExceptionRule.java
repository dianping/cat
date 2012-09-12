package com.dianping.dog.alarm.rule;

import java.util.List;

import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.entity.RuleEntity;
import com.dianping.dog.event.Event;

public class ExceptionRule implements Rule {

	RuleEntity m_entity;
	
	private String reportType;
	
	private String domain;
	
	private String name;
	
	private String type;
	

	public ExceptionRule(RuleEntity entity) {
		m_entity = entity;
	}

	@Override
	public String getName() {
		return m_entity.getName();
	}

	@Override
	public boolean isEligible(Event event) {
		
		return false;
	}

	@Override
	public boolean apply(Event event) {
		
		List<Duration> durations = m_entity.getDurations();
		for (Duration duration : durations) {
			
		}
		return false;
	}

}
