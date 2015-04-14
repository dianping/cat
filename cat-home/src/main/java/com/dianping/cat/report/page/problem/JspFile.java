package com.dianping.cat.report.page.problem;

public enum JspFile {

	ALL("/jsp/report/problem/problemStatics.jsp"),

	DETAIL("/jsp/report/problem/problemDetail.jsp"),

	GROUP("/jsp/report/problem/problemGroup.jsp"),

	HOUR_GRAPH("/jsp/report/problem/problemHourlyGraphs.jsp"),

	HISTORY("/jsp/report/problem/problemHistoryReport.jsp"),

	HISTORY_GRAPH("/jsp/report/problem/problemHistoryGraphs.jsp"),

	MOBILE("/jsp/report/problem/problem_mobile.jsp"),

	THREAD("/jsp/report/problem/problemThread.jsp"),

	GROUP_GRAPHS("/jsp/report/problem/problemHourlyGraphs.jsp"),

	HISTORY_GROUP_GRAPH("/jsp/report/problem/problemHistoryGraphs.jsp"),

	HISTORY_GROUP_REPORT("/jsp/report/problem/problemHistoryGroupReport.jsp"),

	HOURLY_GROUP_REPORT("/jsp/report/problem/problemGroupStatics.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
