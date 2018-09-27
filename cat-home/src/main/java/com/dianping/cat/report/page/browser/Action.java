package com.dianping.cat.report.page.browser;

public enum Action implements org.unidal.web.mvc.Action {
	
	AJAX_LINECHART("view"),

	AJAX_PIECHART("piechart"),

	JS_ERROR("jsError"),
	
	JS_ERROR_DETAIL("jsErrorDetail"),
	
	SPEED("speed"),
	
	SPEED_GRAPH("speedGraph"),
	
	SPEED_CONFIG_FETCH("speedConfigFetch"),
	
	SPEED_JSON("speedJson");

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
