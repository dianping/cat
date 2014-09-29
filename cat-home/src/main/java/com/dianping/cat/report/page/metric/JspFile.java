package com.dianping.cat.report.page.metric;

public enum JspFile {
	METRIC("/jsp/report/metric/metric.jsp"),

	JSON("/jsp/report/metric/json.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
