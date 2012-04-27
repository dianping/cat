package com.dianping.cat.report.page.problem;

public enum JspFile {
	
	GROUP("/jsp/report/problemGroup.jsp"),

	THREAD("/jsp/report/problemThread.jsp"),

	DETAIL("/jsp/report/problemDetail.jsp"),
	
	MOBILE("/jsp/report/problem_mobile.jsp");
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
