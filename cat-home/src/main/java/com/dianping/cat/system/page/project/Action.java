package com.dianping.cat.system.page.project;

public enum Action implements org.unidal.web.mvc.Action {
	ALL("view"),

	PROJECT_UPDATE("update"),

	PROJECT_UPDATE_SUBMIT("updateSubmit");

	public static Action getByName(String name, Action defaultAction) {
		for (Action action : Action.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	private String m_name;

	private Action(String name) {
		m_name = name;
	}

	@Override
	public String getName() {
		return m_name;
	}
}
