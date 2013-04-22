package com.dianping.cat.system.alarm.threshold.event;

import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;
import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventType;

public class ServiceDataEvent implements Event {

	private ThresholdDataEntity m_data;

	public ServiceDataEvent(ThresholdDataEntity data) {
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
		return EventType.ServiceDataEvent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Service: ").append(m_data.toString());
		return sb.toString();
	}

}
