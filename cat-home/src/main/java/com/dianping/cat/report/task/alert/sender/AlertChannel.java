package com.dianping.cat.report.task.alert.sender;

public enum AlertChannel {

	MAIL("mail"),

	SMS("sms"),

	WEIXIN("weixin");

	private String m_name;

	private AlertChannel(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}
}
