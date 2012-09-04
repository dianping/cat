package com.dianping.dog.alarm.rule;

public enum AlarmStrategy {

	EMAIL("email"), SMS("sms");

	private String m_name;

	private int m_interval;

	private AlarmStrategy(String name) {
		m_name = name;
	}

	public AlarmStrategy setInterval(int interval) {
		m_interval = interval;
		return this;
	}

	public String getName() {
		return m_name;
	}

	public int getInterval() {
		return m_interval;
	}

}
