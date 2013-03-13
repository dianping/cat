package com.dianping.cat.report.page.problem;

public enum JspFile {

	ALL("/jsp/report/problemStatics.jsp"),

	DETAIL("/jsp/report/problemDetail.jsp"),

	GROUP("/jsp/report/problemGroup.jsp"),
	
	HOUR_GRAPH("/jsp/report/problemHourlyGraphs.jsp"),

	HISTORY("/jsp/report/problemHistoryReport.jsp"),

	HISTORY_GRAPH("/jsp/report/problemHistoryGraphs.jsp"),

	MOBILE("/jsp/report/problem_mobile.jsp"),

	THREAD("/jsp/report/problemThread.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
