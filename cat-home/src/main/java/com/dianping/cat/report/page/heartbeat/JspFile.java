package com.dianping.cat.report.page.heartbeat;

public enum JspFile {
	VIEW("/jsp/report/heartbeat.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
