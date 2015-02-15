package com.dianping.cat.report.page.storage;

public enum JspFile {
	VIEW("/jsp/report/storage/storage.jsp"),

	HOURL_GRAPH("/jsp/report/storage/hourlyGraphs.jsp"),

	HISTORY_REPORT("/jsp/report/storage/historyStorage.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
