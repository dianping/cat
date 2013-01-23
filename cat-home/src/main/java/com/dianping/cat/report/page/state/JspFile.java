package com.dianping.cat.report.page.state;

public enum JspFile {
	HOURLY("/jsp/report/state.jsp"),

	HISTORY("/jsp/report/stateHistory.jsp"),
	
	GRAPH("/jsp/report/stateGraphs.jsp"),
	
	HISTORY_GRAPH("/jsp/report/stateGraphs.jsp")
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
