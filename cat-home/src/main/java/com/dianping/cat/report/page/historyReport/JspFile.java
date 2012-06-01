package com.dianping.cat.report.page.historyReport;

public enum JspFile {
	TRANSACTION("/jsp/report/historyTransactionReport.jsp"),
	EVENT("/jsp/report/historyEventReport.jsp"),
	PROBLEM("/jsp/report/historyProblemReport.jsp"),
	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
