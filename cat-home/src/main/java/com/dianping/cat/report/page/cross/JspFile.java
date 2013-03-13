package com.dianping.cat.report.page.cross;

public enum JspFile {
	HOURLY_HOST("/jsp/report/crossHost.jsp"),

	HOURLY_METHOD("/jsp/report/crossMethod.jsp"),

	HOURLY_PROJECT("/jsp/report/cross.jsp"),

	HISTORY_HOST("/jsp/report/crossHistoryHost.jsp"),

	HISTORY_METHOD("/jsp/report/crossHistoryMethod.jsp"),

	HISTORY_PROJECT("/jsp/report/crossHistoryProject.jsp"),
	
	METHOD_QUERY("/jsp/report/crossMethodQuery.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
