package com.dianping.cat.report.page.sql;

public enum JspFile {
	VIEW("/jsp/report/sql.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
