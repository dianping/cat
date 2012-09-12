package com.dianping.dog.alarm.rule;

import java.util.List;

import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.alarm.entity.Duration;
import com.dianping.dog.alarm.entity.RuleEntity;

public class ExceptionRule implements Rule{
	
	private RuleEntity m_entity;
	
	@Override
   public boolean init(RuleEntity entity) {
		m_entity = entity;
	   return true;
   }

	@Override
   public String getName() {
	   return m_entity.getName();
   }

	@Override
   public boolean isEligible(DataEvent event) {
	   return false;
   }

	@Override
   public boolean apply(DataEvent event) {
		
		List<Duration> durations = m_entity.getDurations();
		
	   return false;
   }

}
