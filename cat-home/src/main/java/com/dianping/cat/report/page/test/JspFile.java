package com.dianping.cat.report.page.test;

public enum JspFile {
	INSERT("/jsp/report/test/test_insertResult.jsp"),
	QUERYALL("/jsp/report/test/test_queryAll.jsp")
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
