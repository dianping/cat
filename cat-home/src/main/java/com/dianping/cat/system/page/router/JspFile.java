package com.dianping.cat.system.page.router;

public enum JspFile {
	API("/jsp/system/router/api.jsp"),
	
	MODEL("/jsp/system/router/router.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
