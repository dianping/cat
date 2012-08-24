package com.dianping.dog.alarm.rule.reactor;

import com.dianping.dog.alarm.rule.DefaultRuleContext;
import com.dianping.dog.event.Event;

public class ReactorRuleContext<T extends Event> extends DefaultRuleContext {
	private T m_event;

	public ReactorRuleContext(T event) {
		m_event = event;
	}

	public T getEvent() {
		return m_event;
	}
}
