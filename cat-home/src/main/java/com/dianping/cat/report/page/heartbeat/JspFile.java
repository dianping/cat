package com.dianping.cat.report.page.heartbeat;

public enum JspFile {
	HISTORY("/jsp/report/heartbeat/heartbeatHistoryGraph.jsp"),

	PART_HISTORY("/jsp/report/heartbeat/heartbeatPartHistoryGraph.jsp"),

	VIEW("/jsp/report/heartbeat/heartbeat.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
