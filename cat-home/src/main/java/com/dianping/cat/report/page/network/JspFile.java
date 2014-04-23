package com.dianping.cat.report.page.network;

public enum JspFile {
	NETWORK("/jsp/report/network/network.jsp"),
	DASHBOARD("/jsp/report/network/aggregation.jsp");
	
	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
