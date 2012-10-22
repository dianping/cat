package com.dianping.cat.system.alarm.exception.event;

import com.dianping.cat.system.alarm.exception.ExceptionDataEntity;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventType;

public class ExceptionDataEvent implements Event {

	private ExceptionDataEntity m_data;

	public ExceptionDataEntity getData() {
		return m_data;
	}

	public void setData(ExceptionDataEntity data) {
		m_data = data;
	}

	@Override
	public EventType getEventType() {
		return EventType.ExceptionDataEvent;
	}

}
