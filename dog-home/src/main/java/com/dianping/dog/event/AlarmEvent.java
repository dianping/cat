package com.dianping.dog.event;

public class AlarmEvent implements Event {

	private EventType m_type;

	public AlarmEvent() {
		m_type = DefaultEventType.ALARM_EVENT;
	}

	@Override
	public EventType getType() {
		return m_type;
	}

}
