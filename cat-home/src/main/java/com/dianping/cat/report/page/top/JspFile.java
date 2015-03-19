package com.dianping.cat.report.page.top;

public enum JspFile {
	VIEW("/jsp/report/top/top.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
