package com.dianping.cat.report.page.sql;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/sql.jsp"), 
	
	HISTORY_REPORT("/jsp/report/sqlHistory.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
