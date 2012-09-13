package com.dianping.dog.alarm.filter;

import com.dianping.dog.alarm.problem.ProblemEvent;
import com.dianping.dog.alarm.problem.ProblemViolationEvent;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.EventType;
import com.site.lookup.annotation.Inject;

public class ProblemFilterListener implements EventListener{
	
	@Inject
	protected EventDispatcher m_eventdispatcher;

	@Override
   public void onEvent(Event event) {
		ProblemViolationEvent vEvent  = new ProblemViolationEvent((ProblemEvent) event);
		m_eventdispatcher.dispatch(vEvent);
   }

	@Override
   public boolean isEligible(Event event) {
		if(event.getEventType() == EventType.ProblemDataEvent){
	     return true;
		}
		return false;
   }

}
