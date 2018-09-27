package com.dianping.cat.report.page.applog;

public enum JspFile {

	APP_LOG("/jsp/report/applog/appLog.jsp"),

	APP_LOG_DETAIL("/jsp/report/applog/appLogDetail.jsp"),

	APP_LOG_GRAPH("/jsp/report/applog/appLogGraph.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
