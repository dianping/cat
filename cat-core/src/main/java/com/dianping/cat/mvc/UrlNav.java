package com.dianping.cat.mvc;

public enum UrlNav {
	SEVEN_DAY_BEFORE("-7d", -24 * 7),

	ONE_DAY_BEFORE("-1d", -24),

	ONE_HOUR_BEFORE("-1h", -1),

	ONE_HOUR_LATER("+1h", 1),

	ONE_DAY_LATER("+1d", 24),

	SEVEN_DAY_LATER("+7d", 24 * 7);

	private int m_hours;

	private String m_title;

	private UrlNav(String title, int hours) {
		m_title = title;
		m_hours = hours;
	}

	public int getHours() {
		return m_hours;
	}

	public String getTitle() {
		return m_title;
	}
}
