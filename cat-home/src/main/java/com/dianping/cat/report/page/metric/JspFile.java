package com.dianping.cat.report.page.metric;

public enum JspFile {
	METRIC("/jsp/report/metric/metric.jsp"),
	
	DASHBOARD("/jsp/report/metric/dashboard.jsp"),
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
