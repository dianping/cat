package com.dianping.cat.report.heartbeat.heartbeat.page.heartbeat;

public enum JspFile {
	VIEW("/jsp/Heartbeat/heartbeat.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
