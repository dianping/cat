package com.dianping.cat.report.page.cross;

public enum JspFile {
	HOURLY_HOST("/jsp/report/crossHost.jsp"),
	
	HOURLY_METHOD("/jsp/report/crossMethod.jsp"),
	
	HOURLY_PROJECT("/jsp/report/cross.jsp");
	
	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
