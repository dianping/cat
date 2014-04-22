package com.dianping.cat.report.page.externalError;

public enum JspFile {
	VIEW("/jsp/report/externalError.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
