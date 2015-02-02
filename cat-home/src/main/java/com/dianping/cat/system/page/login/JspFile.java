package com.dianping.cat.system.page.login;

public enum JspFile {
	LOGIN("/jsp/system/login.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
