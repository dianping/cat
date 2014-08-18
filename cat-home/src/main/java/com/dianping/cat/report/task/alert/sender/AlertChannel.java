package com.dianping.cat.report.task.alert.sender;

import com.dianping.cat.report.task.alert.AlertConstants;

public enum AlertChannel {

	MAIL(AlertConstants.MAIL),

	SMS(AlertConstants.SMS),

	WEIXIN(AlertConstants.WEIXIN);

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
