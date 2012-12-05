package com.dianping.cat.system.page.project;

public enum JspFile {
	ALL("/jsp/system/project.jsp"), 
	
	UPATE("/jsp/system/projectUpdate.jsp"), ;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
