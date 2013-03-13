package com.dianping.cat.consumer.problem;

public enum ProblemType {
	ERROR("error"),
	
	FAILURE("failure"),

	HEARTBEAT("heartbeat"),

	LONG_SQL("long-sql"),

	LONG_URL("long-url"),

	LONG_SERVICE("long-service"),

	LONG_CACHE("long-cache");

	private String m_name;

	private ProblemType(String name) {
		m_name = name;
	}

	public static ProblemType getByName(String name, ProblemType defaultValue) {
		for (ProblemType action : ProblemType.values()) {
			if (action.getName().equals(name)) {
				return action;
			}
		}

		return defaultValue;
	}

	public String getName() {
		return m_name;
	}
}
