package com.dianping.cat.system.alarm.exception.listener;

import com.dianping.cat.system.alarm.exception.event.ExceptionAlertEvent;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventDispatcher;
import com.dianping.cat.system.event.EventListener;
import com.dianping.cat.system.event.EventType;
import com.site.lookup.annotation.Inject;

public class ExceptionAlertListener implements EventListener {

	@Inject
	private EventDispatcher m_dispatcher;
	
	@Override
	public boolean isEligible(Event event) {
		if (event.getEventType() == EventType.ExceptionAlertEvent) {
			return true;
		}
		return false;
	}

	@Override
	public void onEvent(Event event) {
		//确定状态发出告警
		ExceptionAlertEvent alertEvent = (ExceptionAlertEvent)event;
		alertEvent.toString();
		System.out.println("Send Alarm");
		// TODO Auto-generated method stub
	}

}
