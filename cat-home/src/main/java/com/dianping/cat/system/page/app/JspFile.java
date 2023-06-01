package com.dianping.cat.system.page.app;

public enum JspFile {
	APP_NAME_CHECK("/jsp/system/appConfig/appNameCheck.jsp"),

	APP_LIST("/jsp/system/appConfig/appList.jsp"),

	APP_SPEED_LIST("/jsp/system/appConfig/appSpeedList.jsp"),

	APP_CODE_LIST("/jsp/system/appConfig/code.jsp"),

	APP_CODE_UPDATE("/jsp/system/appConfig/codeUpdate.jsp"),

	APP_SPEED_UPDATE("/jsp/system/appConfig/speedUpdate.jsp"),

	APP_UPDATE("/jsp/system/appConfig/appUpdate.jsp"),
	
	APP_BATCH_ADD("/jsp/system/appConfig/appBatchAdd.jsp"),

	APP_RULE("/jsp/system/appRule/appRule.jsp"),

	APP_RULE_UPDATE("/jsp/system/appRule/appRuleUpdate.jsp"),

	APP_CONFIG_UPDATE("/jsp/system/appConfig/appConfig.jsp"),

	MOBILE_CONFIG_UPDATE("/jsp/system/appConfig/mobileConfigUpdate.jsp"),

	CRASH_LOG_CONFIG_UPDATE("/jsp/system/appConfig/crashLogConfigUpdate.jsp"),
	
	SDK_CONFIG_UPDATE("/jsp/system/appConfig/sdkConfigUpdate.jsp"),

	APP_COMMAND_BATCH("/jsp/system/appConfig/appCommandBatch.jsp"),

	APP_RULE_BATCH("/jsp/system/appConfig/appConfigBatch.jsp"),

	APP_CONSTANTS("/jsp/system/appConfig/constants.jsp"),
	
	APP_SOURCES("/jsp/system/appConfig/appSources.jsp"),

	APP_CONSTANT_UPDATE("/jsp/system/appConfig/constantUpdate.jsp"),

	APP_COMMAND_FORMAT_CONFIG("/jsp/system/appConfig/appCommandFormatConfig.jsp"),

	APP_COMMAND_GROUP("/jsp/system/appConfig/appCommandGroup.jsp"),

	APP_COMMAND_GROUP_ADD("/jsp/system/appConfig/appCommandGroupAdd.jsp"),

	APP_COMMAND_GROUP_UPDATE("/jsp/system/appConfig/appCommandGroupUpdate.jsp"),

	CRASH_RULE_LIST("/jsp/system/appRule/crashRuleList.jsp"),

	CRASH_RULE_UPDATE("/jsp/system/appRule/crashRuleUpdate.jsp"),

	;

	private String m_path;

	private JspFile(String path) {
		m_path = path;
	}

	public String getPath() {
		return m_path;
	}
}
