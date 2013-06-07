package com.dianping.cat.report.page.transaction;

public enum JspFile {
	GRAPHS("/jsp/report/transaction/transactionGraphs.jsp"),

	HISTORY_GRAPH("/jsp/report/transaction/transactionHistoryGraphs.jsp"),

	HISTORY_REPORT("/jsp/report/transaction/transactionHistoryReport.jsp"),

	HOURLY_REPORT("/jsp/report/transaction/transaction.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
