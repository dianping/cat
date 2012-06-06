package com.dianping.cat.report.page.transaction;

public enum JspFile {
	HOURLY_REPORT("/jsp/report/transaction.jsp"),
	
	GRAPHS("/jsp/report/transactionGraphs.jsp"),

	MOBILE("/jsp/report/transactionMobile.jsp"),
	
	MOBILE_GRAPHS("/jsp/report/transactionMobile.jsp"),
	
	HISTORY_REPORT("/jsp/report/transactionHistoryReport.jsp"),
	
	HISTORY_GRAPH("/jsp/report/transactionHistoryGraphs.jsp");
	
	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
