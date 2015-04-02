package com.dianping.cat.report.page;

public enum JspFile {
	VIEW("/jsp/report/page.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
