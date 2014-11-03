package com.dianping.cat.broker.api.app;

public enum AppDataType {
	COMMAND("command"),

	CRASH("crash");

	private String m_name;

	AppDataType(String name) {
		m_name = name;
	}

	public static AppDataType getByName(String name, AppDataType defaultAction) {
		for (AppDataType action : AppDataType.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultAction;
	}

	public String getName() {
		return m_name;
	}

}
