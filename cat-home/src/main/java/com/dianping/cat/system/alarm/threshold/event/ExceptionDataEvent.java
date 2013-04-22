package com.dianping.cat.system.alarm.threshold.event;

import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventType;

public class ExceptionDataEvent implements Event {

	private ThresholdDataEntity m_data;

	public ExceptionDataEvent(ThresholdDataEntity data) {
		m_data = data;
	}

	public ThresholdDataEntity getData() {
		return m_data;
	}

	public void setData(ThresholdDataEntity data) {
		m_data = data;
	}

	@Override
	public EventType getEventType() {
		return EventType.ExceptionDataEvent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Exception: ").append(m_data.toString());
		return sb.toString();
	}

}
