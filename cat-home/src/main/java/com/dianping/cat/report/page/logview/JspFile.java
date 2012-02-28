package com.dianping.cat.report.page.logview;

public enum JspFile {
	LOGVIEW("/jsp/report/logview.jsp"),
	
	LOGVIEW_NO_HEADER("/jsp/report/logview_bare.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
