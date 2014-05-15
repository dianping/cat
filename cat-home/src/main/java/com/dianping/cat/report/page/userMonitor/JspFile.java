package com.dianping.cat.report.page.userMonitor;

public enum JspFile {
	VIEW("/jsp/report/userMonitor.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
