package com.dianping.cat.report.page.browser;

public enum JspFile {
	AJAX_LINECHART("/jsp/report/browser/ajaxLineChart.jsp"),

	AJAX_PIECHART("/jsp/report/browser/ajaxPieChart.jsp"),

	JS_ERROR("/jsp/report/browser/jsError.jsp"),
	
	JS_ERROR_DETAIL("/jsp/report/browser/jsErrorDetail.jsp"),
	
	SPEED("/jsp/report/browser/speed.jsp"),
	
	SPEED_GRAPH("/jsp/report/browser/speedGraph.jsp"),
	
	FETCH_DATA("/jsp/report/browser/fetchData.jsp"),
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
