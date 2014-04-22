package com.dianping.cat.report.page.home;

public enum JspFile {
	VIEW("/jsp/report/home/home.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
