package com.dianping.cat.report.page.cache;

public enum JspFile {
	HISTORY_REPORT("/jsp/report/cacheHistory.jsp"), 
	
	HOURLY_REPORT("/jsp/report/cache.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
