package com.dianping.cat.report.page.system;

public enum JspFile {
	SYSTEM("/jsp/report/system.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
