package com.dianping.cat.report.page.sql;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/sql/sql.jsp"), 
	
	HISTORY_REPORT("/jsp/report/sql/sqlHistory.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
