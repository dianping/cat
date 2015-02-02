package com.dianping.cat.report.page.state;

public enum JspFile {
	HOURLY("/jsp/report/state/state.jsp"),

	HISTORY("/jsp/report/state/stateHistory.jsp"),

	GRAPH("/jsp/report/state/stateGraphs.jsp"),

	HISTORY_GRAPH("/jsp/report/state/stateGraphs.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
