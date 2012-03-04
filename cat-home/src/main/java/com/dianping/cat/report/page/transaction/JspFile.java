package com.dianping.cat.report.page.transaction;

public enum JspFile {
	VIEW("/jsp/report/transaction.jsp"),
	
	GRAPHS("/jsp/report/transaction_graphs.jsp"),
	
	GRAPH("/jsp/report/transaction_graph.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
