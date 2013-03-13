package com.dianping.cat.report.page.query;

public enum JspFile {
	VIEW("/jsp/report/query.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
