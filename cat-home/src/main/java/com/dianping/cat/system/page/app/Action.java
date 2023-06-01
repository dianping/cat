package com.dianping.cat.system.page.app;

public enum Action implements org.unidal.web.mvc.Action {

	APP_CONFIG_UPDATE("appConfigUpdate"),

	MOBILE_CONFIG_UPDATE("mobileConfigUpdate"),
	
	CRASH_LOG_CONFIG_UPDATE("crashLogConfigUpdate"),
	
	SDK_CONFIG_UPDATE("sdkConfigUpdate"),

	APP_NAME_CHECK("appNameCheck"),

	APP_LIST("appList"),

	APP_COMMAND_UPDATE("appUpdate"),

	APP_COMMAND_BATCH_ADD("appBatchAdd"),

	APP_COMMAND_BATCH_SUBMIT("appBatchSubmit"),

	APP_COMMAND_SUBMIT("appSubmit"),

	APP_COMMAND_DELETE("appPageDelete"),

	APP_CODES("appCodes"),

	APP_CODE_UPDATE("appCodeUpdate"),

	APP_CODE_SUBMIT("appCodeSubmit"),

	APP_CODE_ADD("appCodeAdd"),

	APP_CODE_DELETE("appCodeDelete"),

	APP_COMMAND_GROUP("appCommandGroup"),

	APP_COMMAND_GROUP_ADD("appCommandGroupAdd"),

	APP_COMMAND_GROUP_DELETE("appCommandGroupDelete"),

	APP_COMMAND_GROUP_SUBMIT("appCommandGroupSubmit"),

	APP_COMMAND_GROUP_UPDATE("appCommandGroupUpdate"),

	APP_SPEED_LIST("appSpeedList"),

	APP_SPEED_UPDATE("appSpeedUpdate"),

	APP_SPEED_SUBMIT("appSpeedSubmit"),

	APP_SPEED_ADD("appSpeedAdd"),

	APP_SPEED_DELETE("appSpeedDelete"),

	APP_CONSTANTS("appConstants"),

	APP_SOURCES("appSources"),

	APP_SOURCES_SUBMIT("appSourcesSubmit"),

	APP_CONSTANT_ADD("appConstantAdd"),

	APP_CONSTANT_UPDATE("appConstantUpdate"),

	APP_CONSTATN_DELETE("appConstantDelete"),

	APP_CONSTATN_SUBMIT("appConstantSubmit"),

	APP_RULE("appRule"),

	APP_RULE_ADD_OR_UPDATE("appRuleUpdate"),

	APP_RULE_ADD_OR_UPDATE_SUBMIT("appRuleSubmit"),

	APP_RULE_DELETE("appRuleDelete"),

	APP_COMMAND_BATCH("appCommandBatch"),

	APP_COMMAND_BATCH_UPDATE("appCommandBatchUpdate"),

	APP_COMMAND_FORMAT_CONFIG("appCommandFormatConfig"),
	
	CRASH_RULE_LIST("crashRuleList"),

	CRASH_RULE_UPDATE("crashRuleUpdate"),

	CRASH_RULE_DELETE("crashRuleDelete"),

	CRASH_RULE_UPDATE_SUBMIT("crashRuleUpdateSubmit"),;

	private String m_name;

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	private Action(String name) {
		m_name = name;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
