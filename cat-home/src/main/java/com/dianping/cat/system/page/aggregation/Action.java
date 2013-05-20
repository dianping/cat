package com.dianping.cat.system.page.aggregation;

public enum Action implements org.unidal.web.mvc.Action {
	ALL("view"),
	
	UPDATE("update"),

	UPDATE_SUBMIT("updateSubmit"),	
	
	DELETE("delete");

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
