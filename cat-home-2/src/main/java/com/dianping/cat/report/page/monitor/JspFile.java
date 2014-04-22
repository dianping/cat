package com.dianping.cat.report.page.monitor;

public enum JspFile {
	VIEW("/jsp/report/monitor.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
