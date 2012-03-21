package com.dianping.cat.report.page.problem;

public enum JspFile {
	VIEW("/jsp/report/problem.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
