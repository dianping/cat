package com.dianping.cat.report.page.dependency;

public enum JspFile {
	LINE_CHART("/jsp/report/dependency/dependency.jsp"),

	TOPOLOGY("/jsp/report/dependency/dependencyTopologyGraph.jsp"),

	DASHBOARD("/jsp/report/dependency/dependencyDashboard.jsp"),
	
	PRODUCT_LINE("/jsp/report/dependency/dependencyProductLine.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
