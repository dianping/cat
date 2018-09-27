package com.dianping.cat.config;

public enum LogLevel {

	NORMAL(1, "normal"),

	ERROR(2, "error");

	private int m_id;

	private String m_level;

	private LogLevel(int id, String level) {
		m_id = id;
		m_level = level;
	}

	public int getId() {
		return m_id;
	}

	public String getLevel() {
		return m_level;
	}

	public static String getName(int id) {
		for (LogLevel logLevel : LogLevel.values()) {
			if (logLevel.getId() == id) {
				return logLevel.getLevel();
			}
		}

		throw new RuntimeException("Invalid level.");
	}

	public static int getId(String level) {
		for (LogLevel logLevel : LogLevel.values()) {
			if (logLevel.getLevel().equalsIgnoreCase(level)) {
				return logLevel.getId();
			}
		}

		throw new RuntimeException("Invalid level.");
	}
}
