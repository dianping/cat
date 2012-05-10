package com.dianping.cat.report.page.ip;

public enum JspFile {
	VIEW("/jsp/report/ip.jsp"),

	MOBILE("/jsp/report/ip_mobile.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
