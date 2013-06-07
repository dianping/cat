package com.dianping.cat.report.page.database;

public enum JspFile {
	HOURLY("/jsp/report/database/database.jsp"),

	HISTORY("/jsp/report/database/databaseHistory.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
