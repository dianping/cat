package com.dianping.cat.consumer.problem;

public enum ProblemType {
	ERROR("error"),

	FAILURE("failure"),

	LONG_URL("long-url");

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
