package com.dianping.cat.report.page.dashboard;

public enum JspFile {
	VIEW("/jsp/report/dashboard.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
