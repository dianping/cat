package com.dianping.cat.report.page.transaction;

public enum JspFile {
	GRAPHS("/jsp/report/transactionGraphs.jsp"),

	HISTORY_GRAPH("/jsp/report/transactionHistoryGraphs.jsp"),

	HISTORY_REPORT("/jsp/report/transactionHistoryReport.jsp"),

	HOURLY_REPORT("/jsp/report/transaction.jsp"),

	MOBILE("/jsp/report/transactionMobile.jsp"),

	MOBILE_GRAPHS("/jsp/report/transactionMobile.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
