package com.dianping.dog.alarm.filter;

import com.dianping.dog.alarm.problem.ProblemEvent;
import com.dianping.dog.alarm.problem.ProblemViolationEvent;
import com.dianping.dog.event.AbstractReactorListener;

public class ProblemFilterListener extends AbstractReactorListener<ProblemEvent> {
	
	@Override
   public void onEvent(ProblemEvent event) {
		ProblemViolationEvent vEvent  = new ProblemViolationEvent(event);
		m_eventdispatcher.dispatch(vEvent);
   }
	   
}
