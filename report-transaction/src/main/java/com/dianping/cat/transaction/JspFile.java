package com.dianping.cat.transaction;

public enum JspFile {
	VIEW("/jsp/report/transaction.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
