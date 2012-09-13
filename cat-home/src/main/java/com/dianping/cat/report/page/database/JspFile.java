package com.dianping.cat.report.page.database;

public enum JspFile {
	HOURLY("/jsp/report/database.jsp"),

	HISTORY("/jsp/report/databaseHistory.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
