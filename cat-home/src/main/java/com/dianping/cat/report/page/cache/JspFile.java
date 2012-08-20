package com.dianping.cat.report.page.cache;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/cache.jsp"), HISTORY_REPORT("/jsp/report/cacheHistory.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
