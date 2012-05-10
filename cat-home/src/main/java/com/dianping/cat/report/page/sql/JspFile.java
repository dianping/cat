package com.dianping.cat.report.page.sql;

public enum JspFile {
	VIEW("/jsp/report/sql.jsp"),

	GRAPHS("/jsp/report/sql_graphs.jsp"),
	
	MOBILE("/jsp/report/sql_mobile.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
