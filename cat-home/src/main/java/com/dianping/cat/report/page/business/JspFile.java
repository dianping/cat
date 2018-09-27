package com.dianping.cat.report.page.business;

public enum JspFile {
	VIEW("/jsp/report/business/business.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
