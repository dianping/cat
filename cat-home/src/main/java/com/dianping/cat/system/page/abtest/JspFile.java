package com.dianping.cat.system.page.abtest;

public enum JspFile {

	ADDABTEST("/jsp/system/abtest/abtestCreate.jsp"),
	
	ADDGROUPSTRATEGY("/jsp/system/abtest/abtestCreate.jsp"),
	
	PARSEGROUPSTRATEGY("/jsp/system/abtest/abtestGourpStrategy.jsp"),
	
	CREATE("/jsp/system/abtest/abtestCreate.jsp"),

	DETAIL("/jsp/system/abtest/abtestDetail.jsp"),

	VIEW("/jsp/system/abtest/abtestAllTest.jsp"),

	REPORT("/jsp/system/abtest/abtestReport.jsp"),

	MODEL("/jsp/system/abtest/abtestModel.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
