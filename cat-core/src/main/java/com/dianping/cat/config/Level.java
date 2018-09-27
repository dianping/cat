package com.dianping.cat.config;

import java.util.ArrayList;
import java.util.List;

public enum Level {
	ERROR(3, "ERROR"), WARN(2, "WARN"), INFO(1, "INFO"), DEV(0, "DEV");

	private int m_code;

	private String m_name;

	private static List<String> m_levels;

	private Level(int code, String name) {
		m_code = code;
		m_name = name;
	}

	public int getCode() {
		return m_code;
	}

	public void setCode(int code) {
		m_code = code;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public static int getCodeByName(String name) {
		for (Level level : Level.values()) {
			if (level.getName().equals(name)) {
				return level.getCode();
			}
		}
		throw new RuntimeException("Invalid level");
	}

	public static String getNameByCode(int code) {
		for (Level level : Level.values()) {
			if (level.getCode() == code) {
				return level.getName();
			}
		}
		throw new RuntimeException("Invalid level");
	}

	public static List<String> getLevels() {
		if (m_levels == null) {
			m_levels = new ArrayList<String>();
			
			for (Level level : Level.values()) {
				m_levels.add(level.getName());
			}
		}
		return m_levels;
	}

}
