package com.dianping.dog.alarm.rule;

import java.util.List;

import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.entity.RuleEntity;

public class ExceptionRule implements Rule {

	RuleEntity m_entity;

	public ExceptionRule(RuleEntity entity) {
		m_entity = entity;
	}

	@Override
	public String getName() {
		return m_entity.getName();
	}

	@Override
	public boolean isEligible(DataEvent event) {
		String rule_domain=m_entity.getDomain();
		String rule_name=m_entity.getName();
		String rule_type=m_entity.getType();
		String reportType=m_entity.getReportType();
		
		String domain=event.getDomain();
		
		if(!(domain==rule_domain)){
			return false;
		}
		
		
		
		return true;
	}

	@Override
	public boolean apply(DataEvent event) {
		
		List<Duration> durations = m_entity.getDurations();
		for (Duration duration : durations) {
			
		}
		return false;
	}

}
