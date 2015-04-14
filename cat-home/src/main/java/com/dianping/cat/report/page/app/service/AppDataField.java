package com.dianping.cat.report.page.app.service;

import org.unidal.lookup.util.StringUtils;

public enum AppDataField {
	OPERATOR("operator"),

	NETWORK("network"),

	APP_VERSION("app-version"),

	CONNECT_TYPE("connnect-type"),

	PLATFORM("platform"),

	CITY("city"),

	CODE("code");

	private String m_name;

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

	AppDataField(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

}
