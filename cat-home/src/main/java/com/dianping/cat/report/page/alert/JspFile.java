package com.dianping.cat.report.page.alert;

public enum JspFile {
	ALERT("/jsp/report/alert/alertResult.jsp"),

	VIEW("/jsp/report/alert/alertView.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
