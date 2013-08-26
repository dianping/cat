package com.dianping.cat.report.page.bug;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/bug/bug.jsp"),

	HISTORY_REPORT("/jsp/report/bug/bugHistory.jsp"),

	HTTP_JSON("/jsp/report/bug/bugApi.jsp"),

	SERVICE_REPORT("/jsp/report/service/service.jsp"),

	SERVICE_HISTORY_REPORT("/jsp/report/service/serviceHistory.jsp"),

	HEAVY_HISTORY_REPORT("/jsp/report/heavy/heavyHistory.jsp"),

	HEAVY_REPORT("/jsp/report/heavy/heavy.jsp"),

	UTILIZATION_HISTORY_REPORT("/jsp/report/utilization/utilizationHistory.jsp"),

	UTILIZATION_REPORT("/jsp/report/utilization/utilization.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
