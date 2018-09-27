package com.dianping.cat.alarm.spi;

import com.dianping.cat.alarm.spi.AlertChannel;

public enum AlertChannel {

	MAIL("mail"),

	SMS("sms"),

	WEIXIN("weixin"),

	DX("dx");

	private String m_name;

	public static AlertChannel findByName(String name) {
		for (AlertChannel channel : values()) {
			if (channel.getName().equals(name)) {
				return channel;
			}
		}
		return null;
	}

	private AlertChannel(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}
}
