package com.dianping.cat.report.page.browser.service;

import org.unidal.lookup.util.StringUtils;

public enum AjaxDataField {
	OPERATOR("operator"),

	CITY("city"),

	CODE("code"),
	
	NETWORK("network");

	private String m_name;

	public static AjaxDataField getByName(String name, AjaxDataField defaultField) {
		if (StringUtils.isNotEmpty(name)) {
			for (AjaxDataField field : AjaxDataField.values()) {
				if (field.getName().equals(name)) {
					return field;
				}
			}
		}
		return defaultField;
	}

	AjaxDataField(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

}
