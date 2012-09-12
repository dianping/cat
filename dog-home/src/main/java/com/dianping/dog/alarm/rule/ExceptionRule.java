package com.dianping.dog.alarm.rule;

import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.alarm.entity.RuleEntity;

<<<<<<< HEAD
public class ExceptionRule implements Rule {

	RuleEntity m_entity;
	
	private String reportType;
=======
public class ExceptionRule implements Rule{
	
	private RuleEntity m_entity;
>>>>>>> 854fa5bb6a802ca4c6ab6325e495912ef0a1c662
	
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
<<<<<<< HEAD
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
=======
   public boolean isEligible(DataEvent event) {
	   return false;
   }

	@Override
   public boolean apply(DataEvent event) {
		
		
	   return false;
   }
>>>>>>> 854fa5bb6a802ca4c6ab6325e495912ef0a1c662

}
