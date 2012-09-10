package com.dianping.dog.alarm.problem;

import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventType;

public class ProblemViolationEvent implements Event {
	private ProblemEvent m_event;

	public ProblemViolationEvent(ProblemEvent event) {
		m_event = event;
	}

	public ProblemEvent getOrigin() {
		return m_event;
	}

	@Override
	public EventType getEventType() {
		return EventType.ProblemViolationEvent;
	}
}
