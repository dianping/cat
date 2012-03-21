package com.dianping.cat.report.page.ip;

public enum JspFile {
	VIEW("/jsp/report/ip.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
