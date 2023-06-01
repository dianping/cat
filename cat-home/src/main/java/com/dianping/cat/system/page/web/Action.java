package com.dianping.cat.system.page.web;

public enum Action implements org.unidal.web.mvc.Action {

	CODE_UPDATE("codeUpdate"),

	CODE_SUBMIT("codeSubmit"),

	CODE_DELETE("codeDelete"),

	CODE_LIST("codeList"),

	SPEED_UPDATE("speedUpdate"),

	SPEED_SUBMIT("speedSubmit"),

	SPEED_DELETE("speedDelete"),

	SPEED_LIST("speedList"),

	JS_RULE_LIST("jsRuleList"),

	JS_RULE_UPDATE("jsRuleUpdate"),

	JS_RULE_DELETE("jsRuleDelete"),

	JS_RULE_UPDATE_SUBMIT("jsRuleUpdateSubmit"),

	WEB_RULE("webRule"),

	WEB_RULE_ADD_OR_UPDATE("webRuleUpdate"),

	WEB_RULE_ADD_OR_UPDATE_SUBMIT("webRuleSubmit"),

	WEB_RULE_DELETE("webRuleDelete"),

	WEB_CONSTANTS("webConstants"),

	URL_PATTERN_ALL("urlPatterns"),

	URL_PATTERN_CONFIG_UPDATE("urlPatternConfigUpdate"),

	URL_PATTERN_UPDATE("urlPatternUpdate"),

	URL_PATTERN_UPDATE_SUBMIT("urlPatternUpdateSubmit"),

	URL_PATTERN_DELETE("urlPatternDelete"), 
	
	WEB_CONFIG_UPDATE("webConfigUpdate"),
	
	WEB_SPEED_CONFIG_UPDATE("webSpeedConfigUpdate");

	private String m_name;

	private Action(String name) {
		m_name = name;
	}

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
