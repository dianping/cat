package com.dianping.cat.report.page.app;

public enum JspFile {
	VIEW("/jsp/report/app/linechart.jsp"),

	PIECHART("/jsp/report/app/piechart.jsp"),

	CONN_LINECHART("/jsp/report/app/connLinechart.jsp"),

	CONN_PIECHART("/jsp/report/app/connPiechart.jsp"),

	APP_MODIFY_RESULT("/jsp/report/app/result.jsp"),

	APP_FETCH_DATA("/jsp/report/app/fetchData.jsp"),

	CRASH_LOG("/jsp/report/app/crashLog.jsp"),

	SPEED("/jsp/report/app/speed.jsp"),

	STATISTICS("/jsp/report/app/statistics.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
