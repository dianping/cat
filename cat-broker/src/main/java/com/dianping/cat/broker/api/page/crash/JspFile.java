package com.dianping.cat.broker.api.page.crash;

public enum JspFile {
	VIEW("/jsp/api/crash.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
