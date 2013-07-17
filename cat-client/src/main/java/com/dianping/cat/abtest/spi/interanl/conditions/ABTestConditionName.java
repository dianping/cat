package com.dianping.cat.abtest.spi.interanl.conditions;

public enum ABTestConditionName {

	URL("url"),

	TRAFFIC_PERCENT("percent");

	public static ABTestConditionName getByName(String name, ABTestConditionName defaultName) {
		for (ABTestConditionName filterName : ABTestConditionName.values()) {
			if (filterName.m_name == name) {
				return filterName;
			}
		}

		return defaultName;
	}

	private String m_name;

	private ABTestConditionName(String name) {
		m_name = name;
	}
}
