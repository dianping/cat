package com.dianping.cat.system.page.abtest;

public enum JspFile {

	VIEW("/jsp/system/abtest/abtestAllTest.jsp"),

	CREATE("/jsp/system/abtest/abtestCreate.jsp"),

	DETAIL("/jsp/system/abtest/abtestDetail.jsp"),

	REPORT("/jsp/system/abtest/abtestReport.jsp"),

	MODEL("/jsp/system/abtest/abtestModel.jsp"),

	AJAX("/jsp/system/abtest/abtestAjax.jsp"),
	
	ABTEST_CACULATOR("/jsp/system/abtest/abtestCaculator.jsp"),
	
	SCRIPT_FRAGEMENT("/jsp/system/abtest/scriptFragement.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
