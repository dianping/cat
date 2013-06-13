package com.dianping.cat.report.page.metric;

public enum JspFile {
	VIEW("/jsp/report/metric.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
