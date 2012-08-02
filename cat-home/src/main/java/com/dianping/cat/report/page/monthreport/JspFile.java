package com.dianping.cat.report.page.monthreport;

public enum JspFile {
	VIEW("/jsp/report/monthreport.jsp"),

	ALL("/jsp/report/monthreportAll.jsp"),
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
