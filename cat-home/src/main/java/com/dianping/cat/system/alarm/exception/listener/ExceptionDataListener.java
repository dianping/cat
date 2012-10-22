package com.dianping.cat.system.alarm.exception.listener;

import com.dianping.cat.system.alarm.exception.event.ExceptionAlertEvent;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventDispatcher;
import com.dianping.cat.system.event.EventListener;
import com.dianping.cat.system.event.EventType;
import com.site.lookup.annotation.Inject;

public class ExceptionDataListener implements EventListener {

	@Inject
	private EventDispatcher m_dispatcher;

	@Override
	public boolean isEligible(Event event) {
		if (event.getEventType() == EventType.ExceptionDataEvent) {
			return true;
		}
		return false;
	}

	@Override
	public void onEvent(Event event) {
		System.out.println("Get Data");

		ExceptionAlertEvent alertEvent = new ExceptionAlertEvent();
		m_dispatcher.dispatch(alertEvent);
		// 匹配数据，发出告警
		// TODO Auto-generated method stub

	}

}
