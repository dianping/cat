package com.dianping.cat.system.page.business;

public enum JspFile {
	VIEW("/jsp/system/business/list.jsp"),
	
	ADD("/jsp/system/business/add.jsp"),
	
	TAG("/jsp/system/business/tag.jsp"),
	
	AlertAdd("/jsp/system/business/alertAdd.jsp"),
	
	CustomAdd("/jsp/system/business/customAdd.jsp")

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
