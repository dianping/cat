package com.dianping.cat.agent.core.page.index;

public enum JspFile {
	VIEW("/jsp/core/index.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
