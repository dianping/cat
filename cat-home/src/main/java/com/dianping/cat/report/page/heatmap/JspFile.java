package com.dianping.cat.report.page.heatmap;

public enum JspFile {
	JSONP("/jsp/report/heatmap_jsonp.jsp"),

	VIEW("/jsp/report/heatmap.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
