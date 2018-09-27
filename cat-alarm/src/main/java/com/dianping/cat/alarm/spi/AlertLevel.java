package com.dianping.cat.alarm.spi;

import com.dianping.cat.alarm.spi.AlertLevel;

public enum AlertLevel {

	WARNING("warning", 1),

	ERROR("error", 2);

	private String m_level;

	private int m_priority;

	private AlertLevel(String level, int priority) {
		m_level = level;
		m_priority = priority;
	}

	public static AlertLevel findByName(String level) {
		for (AlertLevel tmp : values()) {
			if (tmp.getLevel().equals(level)) {
				return tmp;
			}
		}
		return WARNING;
	}

	public String getLevel() {
		return m_level;
	}

	public int getPriority() {
		return m_priority;
	}
}
