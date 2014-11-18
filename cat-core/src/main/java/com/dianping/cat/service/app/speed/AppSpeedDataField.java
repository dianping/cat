package com.dianping.cat.service.app.speed;

import com.site.lookup.util.StringUtils;

public enum AppSpeedDataField {
	OPERATOR("operator"),

	NETWORK("network"),

	APP_VERSION("app-version"),

	PLATFORM("platform"),

	CITY("city");

	private String m_name;

	public static AppSpeedDataField getByName(String name, AppSpeedDataField defaultField) {
		if (StringUtils.isNotEmpty(name)) {
			for (AppSpeedDataField field : AppSpeedDataField.values()) {
				if (field.getName().equals(name)) {
					return field;
				}
			}
		}
		return defaultField;
	}

	AppSpeedDataField(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

}
