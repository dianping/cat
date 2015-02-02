package com.dianping.cat.report.page.matrix;

public enum JspFile {
	VIEW("/jsp/report/matrix/matrix.jsp"),

	HISTORY_REPORT("/jsp/report/matrix/matrixHistoryReport.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
