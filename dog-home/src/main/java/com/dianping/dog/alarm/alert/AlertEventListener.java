package com.dianping.dog.alarm.alert;

import com.dianping.dog.alarm.problem.AlertEvent;
import com.dianping.dog.alarm.rule.message.Message;
import com.dianping.dog.alarm.rule.message.MessageCreater;
import com.dianping.dog.alarm.rule.message.MessageCreaterFactory;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventListener;
import com.dianping.dog.event.EventType;
import com.site.lookup.annotation.Inject;

public class AlertEventListener implements EventListener {
	
	@Inject
	private MessageCreaterFactory factory;

	@Override
   public boolean isEligible(Event event) {
		if(EventType.ProblemAlarmEvent == event.getEventType()){
			return true;
		}
	   return false;
   }

	@Override
   public void onEvent(Event event) {
	   AlertEvent alertEvent = (AlertEvent) event;
	   MessageCreater messageCreater = factory.getMessageCreater(alertEvent);
	   Message  message = messageCreater.create(alertEvent);
	   
   }

}
