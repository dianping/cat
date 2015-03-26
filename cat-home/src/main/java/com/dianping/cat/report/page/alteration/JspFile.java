package com.dianping.cat.report.page.alteration;

public enum JspFile {
	INSERT("/jsp/report/alteration/alter_insertResult.jsp"),

	VIEW("/jsp/report/alteration/alter_view.jsp"), ;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
