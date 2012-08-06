package com.dianping.cat.report.page.ip;

public enum JspFile {
	MOBILE("/jsp/report/ip_mobile.jsp"),

	MOBILE_IP("/jsp/report/ip_mobile.jsp"),

	VIEW("/jsp/report/ip.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
