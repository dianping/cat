package com.dianping.cat.report.page.app;

public enum JspFile {
	VIEW("/jsp/report/app/linechart.jsp"),

	PIECHART("/jsp/report/app/piechart.jsp"),

	CONN_LINECHART("/jsp/report/app/connLinechart.jsp"),

	CONN_PIECHART("/jsp/report/app/connPiechart.jsp"),

	APP_MODIFY_RESULT("/jsp/report/app/result.jsp"),

	APP_FETCH_DATA("/jsp/report/app/fetchData.jsp"),

	SPEED("/jsp/report/app/speed.jsp"),

	SPEED_GRAPH("/jsp/report/app/speedGraph.jsp"),
	
	DASHBOARD("/jsp/report/app/dashboard.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
