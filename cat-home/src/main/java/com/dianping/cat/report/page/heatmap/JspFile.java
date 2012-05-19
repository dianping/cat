package com.dianping.cat.report.page.heatmap;

public enum JspFile {
	VIEW("/jsp/report/heatmap.jsp"),

	JSONP("/jsp/report/heatmap_jsonp.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
