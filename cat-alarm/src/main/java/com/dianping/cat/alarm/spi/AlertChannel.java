package com.dianping.cat.alarm.spi;

public enum AlertChannel {

	MAIL("mail"),

	SMS("sms"),

	WEIXIN("weixin"),

	DX("dx");

	private String m_name;

	private AlertChannel(String name) {
		m_name = name;
	}

	public static AlertChannel findByName(String name) {
		for (AlertChannel channel : values()) {
			if (channel.getName().equals(name)) {
				return channel;
			}
		}
		return null;
	}

	public String getName() {
		return m_name;
	}
}
