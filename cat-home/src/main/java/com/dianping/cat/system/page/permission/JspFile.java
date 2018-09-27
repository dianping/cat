package com.dianping.cat.system.page.permission;

public enum JspFile {

	USER("/jsp/system/permission/userConfigUpdate.jsp"),

	RESOURCE("/jsp/system/permission/resourceConfigUpdate.jsp"),

	ERROR("/jsp/system/permission/error.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
