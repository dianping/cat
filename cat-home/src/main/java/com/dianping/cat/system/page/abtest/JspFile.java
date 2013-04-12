package com.dianping.cat.system.page.abtest;

public enum JspFile {
	VIEW("/jsp/system/abtest.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
