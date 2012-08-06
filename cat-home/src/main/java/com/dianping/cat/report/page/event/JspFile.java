package com.dianping.cat.report.page.event;

public enum JspFile {
	GRAPHS("/jsp/report/event_graphs.jsp"),

	HISTORY_GRAPH("/jsp/report/eventHistoryGraphs.jsp"),

	HISTORY_REPORT("/jsp/report/eventHistoryReport.jsp"),

	HOURLY_REPORT("/jsp/report/event.jsp"),

	MOBILE("/jsp/report/event_mobile.jsp"),

	MOBILE_GRAPHS("/jsp/report/event_mobile.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
