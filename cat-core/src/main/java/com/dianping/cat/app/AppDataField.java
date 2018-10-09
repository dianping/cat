package com.dianping.cat.app;

import org.unidal.lookup.util.StringUtils;

public enum AppDataField {
	OPERATOR("operator", "运营商"),

	NETWORK("network", "网络类型"),

	APP_VERSION("app-version", "版本"),

	CONNECT_TYPE("connect-type", "连接类型"),

	PLATFORM("platform", "平台"),

	SOURCE("source", "来源"),

	CITY("city", "城市"),

	CODE("code", "返回码");

	private String m_name;

	private String m_title;

	public static AppDataField getByName(String name, AppDataField defaultField) {
		if (StringUtils.isNotEmpty(name)) {
			for (AppDataField field : AppDataField.values()) {
				if (field.getName().equals(name)) {
					return field;
				}
			}
		}
		return defaultField;
	}

	public static AppDataField getByTitle(String title) {
		if (StringUtils.isNotEmpty(title)) {
			for (AppDataField field : AppDataField.values()) {
				if (field.getTitle().equals(title)) {
					return field;
				}
			}
		}
		return null;
	}

	AppDataField(String name, String title) {
		m_name = name;
		m_title = title;
	}

	public String getName() {
		return m_name;
	}

	public String getTitle() {
		return m_title;
	}

}
