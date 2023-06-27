package com.dianping.cat.system.page.web;

public enum JspFile {
	VIEW("/jsp/system/webconfig.jsp"),
	
	CODE_UPDATE("/jsp/system/webRule/codeUpdate.jsp"),
	
	CODE_LIST("/jsp/system/webRule/code.jsp"),

	SPEED_UPDATE("/jsp/system/webRule/speedUpdate.jsp"),

	SPEED_LIST("/jsp/system/webRule/speed.jsp"),
	
	JS_RULE_LIST("/jsp/system/webRule/jsRuleList.jsp"),

	JS_RULE_UPDATE("/jsp/system/webRule/jsRuleUpdate.jsp"),
	
	WEB_RULE("/jsp/system/webRule/webRule.jsp"),

	WEB_RULE_UPDATE("/jsp/system/webRule/webRuleUpdate.jsp"),

	WEB_CONSTANTS_LIST("/jsp/system/webRule/webConstantsList.jsp"),
	
	URL_PATTERN_ALL("/jsp/system/urlPattern/urlPattern.jsp"),

	URL_PATTERN_CONFIG_UPDATE("/jsp/system/urlPattern/urlPatternConfig.jsp"),

	URL_PATTERN_UPATE("/jsp/system/urlPattern/urlPatternUpdate.jsp"),
	
	WEB_CONFIG_UPDATE("/jsp/system/webRule/webConfig.jsp"),
	
	WEB_SPEED_CONFIG_UPDATE("/jsp/system/webRule/webSpeedConfig.jsp");

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
