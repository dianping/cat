package com.dianping.dog.alarm.rule;

<<<<<<< HEAD
import com.dianping.dog.event.Event;
=======
import com.dianping.dog.alarm.data.DataEvent;
import com.dianping.dog.alarm.entity.RuleEntity;
>>>>>>> 854fa5bb6a802ca4c6ab6325e495912ef0a1c662

public interface Rule {
	
	public String getName();
	
	public boolean isEligible(DataEvent event);

	public boolean apply(DataEvent event);
			
}
