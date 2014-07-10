package com.dianping.cat.report.page.summary;

public enum JspFile {
	VIEW("/jsp/report/summary/summary_view.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
