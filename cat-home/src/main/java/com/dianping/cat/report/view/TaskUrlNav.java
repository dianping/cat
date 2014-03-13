package com.dianping.cat.report.view;

public enum TaskUrlNav {
	ONE_DAY_BEFORE("-1d", -24),

	ONE_DAY_LATER("+1d", +24);

	private int m_hours;

	private String m_title;

	private TaskUrlNav(String title, int hours) {
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
