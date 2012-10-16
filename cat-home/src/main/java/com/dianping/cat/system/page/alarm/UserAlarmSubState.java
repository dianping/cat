package com.dianping.cat.system.page.alarm;

import java.util.Comparator;

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

	public static class UserAlarmSubStateCompartor implements Comparator<UserAlarmSubState> {

		@Override
		public int compare(UserAlarmSubState o1, UserAlarmSubState o2) {
			int sub1 = o1.getSubscriberState();
			int sub2 = o2.getSubscriberState();
			
			if (sub1 != sub2) {
				return sub2 - sub1;
			}
			return o1.getAlarmRule().getDomain().compareTo(o2.getAlarmRule().getDomain());
		}
	}
	
}
