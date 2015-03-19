package com.dianping.cat.report.page.statistics;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/bug/bug.jsp"),

	HISTORY_REPORT("/jsp/report/bug/bugHistory.jsp"),

	HTTP_JSON("/jsp/report/bug/bugApi.jsp"),

	SERVICE_REPORT("/jsp/report/service/service.jsp"),

	SERVICE_HISTORY_REPORT("/jsp/report/service/serviceHistory.jsp"),

	HEAVY_HISTORY_REPORT("/jsp/report/heavy/heavyHistory.jsp"),

	HEAVY_REPORT("/jsp/report/heavy/heavy.jsp"),

	UTILIZATION_HISTORY_REPORT("/jsp/report/utilization/utilizationHistory.jsp"),

	UTILIZATION_REPORT("/jsp/report/utilization/utilization.jsp"),

	BROWSER_HISTORY_REPORT("/jsp/report/browser/browserHistory.jsp"),

	BROWSER_REPORT("/jsp/report/browser/browser.jsp"),

	ALERT_HISTORY_REPORT("/jsp/report/exceptionAlert/alertHistory.jsp"),

	ALERT_REPORT_DETAIL("/jsp/report/exceptionAlert/exceptionDetail.jsp"),

	ALERT_REPORT("/jsp/report/exceptionAlert/alert.jsp"),

	ALERT_SUMMARY("/jsp/report/summary/summary.jsp"),

	JAR_REPORT("/jsp/report/jar/jar.jsp"),

	SYSTEM_REPORT("/jsp/report/statistics/system.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
