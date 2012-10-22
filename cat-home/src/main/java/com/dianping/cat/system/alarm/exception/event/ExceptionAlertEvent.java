package com.dianping.cat.system.alarm.exception.event;

import com.dianping.cat.system.event.Event;
import com.dianping.cat.system.event.EventType;

public class ExceptionAlertEvent implements Event {

	private int m_ruleId;

	private String m_ruleType;

	public String getRuleType() {
		return m_ruleType;
	}

	public void setRuleType(String ruleType) {
		m_ruleType = ruleType;
	}

	public int getRuleId() {
		return m_ruleId;
	}

	public void setRuleId(int ruleId) {
		m_ruleId = ruleId;
	}

	@Override
	public EventType getEventType() {
		return EventType.ExceptionAlertEvent;
	}

}
