package com.dianping.cat.report.page.appstats;

public enum JspFile {
	VIEW("/jsp/report/appstats/statistics.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
