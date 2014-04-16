package com.dianping.cat.report.page.test;

public enum JspFile {
	INSERT_VIEW("/jsp/report/test_insert.jsp"),
	QUERY_VIEW("/jsp/report/test_query.jsp"),
	XML_VIEW("/jsp/report/test_model.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
