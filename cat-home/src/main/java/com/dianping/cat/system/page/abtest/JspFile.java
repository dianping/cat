package com.dianping.cat.system.page.abtest;

public enum JspFile {
	VIEW("/jsp/system/abtest.jsp"),
	
	DOCREATE("/jsp/system/abtest.jsp"),

	LIST("/jsp/system/abtestAllTest.jsp"),
	
	REPORT("/jsp/system/abtestReport.jsp")
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
