package com.dianping.cat.report.page.event;

public enum JspFile {
	VIEW("/jsp/report/event.jsp"),
	
	GRAPHS("/jsp/report/event_graphs.jsp"),

	MOBILE("/jsp/report/event_mobile.jsp");
	
	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
