package com.dianping.cat.report.page.app;

public enum JspFile {
	VIEW("/jsp/report/app/linechart.jsp"),

	PIECHART("/jsp/report/app/piechart.jsp"),

	APP_MODIFY_RESULT("/jsp/report/app/result.jsp"),

	APP_FETCH_DATA("/jsp/report/app/fetchData.jsp"),

	CRASH_LOG("/jsp/report/app/crashLog.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
