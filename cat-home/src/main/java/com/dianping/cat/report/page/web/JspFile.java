package com.dianping.cat.report.page.web;

public enum JspFile {
	VIEW("/jsp/report/web/web.jsp"),

	PIECHART("/jsp/report/web/piechart.jsp"),

	JSON("/jsp/report/web/json.jsp"),

	PROBLEM("/jsp/report/web/problem.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
