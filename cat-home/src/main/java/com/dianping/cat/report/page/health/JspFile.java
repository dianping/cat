package com.dianping.cat.report.page.health;

public enum JspFile {
	HOURLY("/jsp/report/health.jsp"),

	HISTORY("/jsp/report/healthHistory.jsp"),

	HISTORY_GRAPH("/jsp/report/healthGraphs.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
