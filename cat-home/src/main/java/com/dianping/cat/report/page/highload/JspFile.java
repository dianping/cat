package com.dianping.cat.report.page.highload;

public enum JspFile {
	VIEW("/jsp/report/highload/highload.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
