package com.dianping.cat.report.page.logview;

public enum JspFile {
	LOGVIEW("/jsp/report/logview.jsp"),

	LOGVIEW_NO_HEADER("/jsp/report/logview_no_header.jsp"),

	MOBILE("/jsp/report/logview_mobile.jsp"), 
	
	DETAIL("/jsp/report/logview_detail.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
