package com.dianping.cat.report.view;

public enum TaskUrlNav {
	ONE_DAY_BEFORE("-1d", -24),

	ONE_DAY_LATER("+1d", +24);


	private TaskUrlNav(String title, int hours) {
		m_title = title;
		m_hours = hours;
	}

	private String m_title;

	private int m_hours;

	public String getTitle() {
		return m_title;
	}

	public int getHours() {
		return m_hours;
	}
}
