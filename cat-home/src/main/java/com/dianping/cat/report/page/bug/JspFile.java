package com.dianping.cat.report.page.bug;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/bug/bug.jsp"),

	HISTORY_REPORT("/jsp/report/bug/bugHistory.jsp"),

	HTTP_JSON("/jsp/report/bug/bugApi.jsp"),

	SERVICE_REPORT("/jsp/report/service/service.jsp"),

	SERVICE_HISTORY_REPORT("/jsp/report/service/serviceHistory.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
