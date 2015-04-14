package com.dianping.cat.report.page.network;

public enum JspFile {
	NETWORK("/jsp/report/network/metric.jsp"), NETTOPOLOGY("/jsp/report/network/nettopology.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
