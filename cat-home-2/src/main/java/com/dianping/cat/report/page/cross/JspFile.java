package com.dianping.cat.report.page.cross;

public enum JspFile {
	HOURLY_HOST("/jsp/report/cross/crossHost.jsp"),

	HOURLY_METHOD("/jsp/report/cross/crossMethod.jsp"),

	HOURLY_PROJECT("/jsp/report/cross/cross.jsp"),

	HISTORY_HOST("/jsp/report/cross/crossHistoryHost.jsp"),

	HISTORY_METHOD("/jsp/report/cross/crossHistoryMethod.jsp"),

	HISTORY_PROJECT("/jsp/report/cross/crossHistoryProject.jsp"),

	METHOD_QUERY("/jsp/report/cross/crossMethodQuery.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
