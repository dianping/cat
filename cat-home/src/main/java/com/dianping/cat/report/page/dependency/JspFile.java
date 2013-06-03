package com.dianping.cat.report.page.dependency;

public enum JspFile {
	VIEW("/jsp/report/dependency.jsp"),

	GRAPH("/jsp/report/dependencyTopologyGraph.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
