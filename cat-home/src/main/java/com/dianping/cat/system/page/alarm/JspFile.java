package com.dianping.cat.system.page.alarm;

public enum JspFile {
	VIEW("/jsp/system/alarm.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
