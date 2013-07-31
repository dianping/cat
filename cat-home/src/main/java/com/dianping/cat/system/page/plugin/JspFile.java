package com.dianping.cat.system.page.plugin;

public enum JspFile {
	VIEW("/jsp/system/plugin/plugin.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
