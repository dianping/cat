package com.dianping.cat.system.page.alarm;

import com.dianping.cat.home.dal.alarm.AlarmRule;

public class UserAlarmSubState {

	private AlarmRule m_alarmRule;

	private int m_subscriberState;

	public UserAlarmSubState(AlarmRule alarmRule) {
		m_alarmRule = alarmRule;
	}

	public AlarmRule getAlarmRule() {
		return m_alarmRule;
	}

	public int getSubscriberState() {
		return m_subscriberState;
	}

	public void setAlarmRule(AlarmRule alarmRule) {
		m_alarmRule = alarmRule;
	}

	public void setSubscriberState(int subscriberState) {
		m_subscriberState = subscriberState;
	}

}
