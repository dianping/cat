package com.dianping.cat.report.page.test;

public enum JspFile {
	INSERT_VIEW("/jsp/report/test.jsp"),
	QUERY_VIEW("/jsp/report/test2.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
