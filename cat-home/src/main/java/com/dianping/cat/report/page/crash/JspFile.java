package com.dianping.cat.report.page.crash;

public enum JspFile {
	APP_CRASH_LOG("/jsp/report/crash/appCrashLog.jsp"),

	APP_CRASH_LOG_DETAIL("/jsp/report/crash/appCrashLogDetail.jsp"),

	APP_CRASH_GRAPH("/jsp/report/crash/appCrashGraph.jsp"),

	APP_CRASH_TREND("/jsp/report/crash/appCrashTrend.jsp"),

	APP_FETCH_DATA("/jsp/report/crash/fetchData.jsp"),

	CRASH_STATISTICS("/jsp/report/crash/crashStatistics.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
