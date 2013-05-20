package com.dianping.cat.system.page.aggregation;

public enum JspFile {
	ALL("/jsp/system/aggregation.jsp"),
	
	UPATE("/jsp/system/aggregationUpdate.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
