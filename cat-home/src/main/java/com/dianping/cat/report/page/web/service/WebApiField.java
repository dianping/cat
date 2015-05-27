package com.dianping.cat.report.page.web.service;

import org.unidal.lookup.util.StringUtils;

public enum WebApiField {
	OPERATOR("operator"),

	CITY("city"),

	CODE("code");

	private String m_name;

	public static WebApiField getByName(String name, WebApiField defaultField) {
		if (StringUtils.isNotEmpty(name)) {
			for (WebApiField field : WebApiField.values()) {
				if (field.getName().equals(name)) {
					return field;
				}
			}
		}
		return defaultField;
	}

	WebApiField(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

}
