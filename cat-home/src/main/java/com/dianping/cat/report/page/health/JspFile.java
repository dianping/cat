package com.dianping.cat.report.page.health;

public enum JspFile {
	HOURLY("/jsp/report/health/health.jsp"),

	HISTORY("/jsp/report/health/healthHistory.jsp"),

	HISTORY_GRAPH("/jsp/report/health/healthGraphs.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
