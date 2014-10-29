package com.dianping.cat.report.page.web;

public enum JspFile {
	VIEW("/jsp/report/web/web.jsp"),
	JSON("/jsp/report/web/json.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
