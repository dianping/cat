package com.dianping.cat.config.app;

import com.site.lookup.util.StringUtils;

public enum AppDataGroupByField {
	OPERATOR("operator"), NETWORK("network"), APP_VERSION("app-version"), CONNECT_TYPE("connnect-type"), PLATFORM(
	      "platform"), CITY("city"), CODE("code");

	private String m_name;

	public static AppDataGroupByField getByName(String name, AppDataGroupByField defaultField) {
		if (StringUtils.isNotEmpty(name)) {
			for (AppDataGroupByField field : AppDataGroupByField.values()) {
				if (field.getName().equals(name)) {
					return field;
				}
			}
		}
		return defaultField;
	}

	AppDataGroupByField(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

}
