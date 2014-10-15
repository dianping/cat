package com.dianping.cat.report.page.app;

public enum JspFile {
	VIEW("/jsp/report/app/appLinechart.jsp"),
	PIECHART("/jsp/report/app/appPiechart.jsp"),
	APP_MODIFY_RESULT("/jsp/report/app/result.jsp"),
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
