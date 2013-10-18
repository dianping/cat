package com.dianping.cat.report.page.metric;

public enum MetricDate {
	CURRENT(0, "当前时段"),

	LAST_DAY(-1, "前一天相同时段"),

	LAST_WEEK(-7, "上周相同时段");

	private int m_index;

	private String m_title;

	private MetricDate(int index, String title) {
		m_index = index;
		m_title = title;
	}

	public int getIndex() {
		return m_index;
	}

	public String getTitle() {
		return m_title;
	}

}
