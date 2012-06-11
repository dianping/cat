package com.dianping.cat.report.page.event;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/event.jsp"),

	HISTORY_REPORT("/jsp/report/eventHistoryReport.jsp"),
	
	HISTORY_GRAPH("/jsp/report/eventHistoryGraphs.jsp"),

	GRAPHS("/jsp/report/event_graphs.jsp"),

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
