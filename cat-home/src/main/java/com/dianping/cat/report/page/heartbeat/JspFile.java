package com.dianping.cat.report.page.heartbeat;

public enum JspFile {
	HISTORY("/jsp/report/heartbeatHistoryGraph.jsp"),
	
	PART_HISTORY("/jsp/report/heartbeatPartHistoryGraph.jsp"),

	MOBILE("/jsp/report/heartbeat_mobile.jsp"),

	VIEW("/jsp/report/heartbeat.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
