package com.dianping.dog.alarm.problem;

import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventType;

public class ProblemViolationEvent implements Event {
	private ProblemDataEvent m_event;

	public ProblemViolationEvent(ProblemDataEvent event) {
		m_event = event;
	}

	public ProblemDataEvent getOrigin() {
		return m_event;
	}

	@Override
	public EventType getEventType() {
		return EventType.ProblemViolationEvent;
	}
}
