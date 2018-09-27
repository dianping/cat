package com.dianping.cat.system.page.project;

public enum JspFile {
	JSON("/jsp/system/project/json.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
