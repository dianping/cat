package com.dianping.cat.report.page.overload;

public enum JspFile {
	VIEW("/jsp/report/overload/overload.jsp"), ;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
