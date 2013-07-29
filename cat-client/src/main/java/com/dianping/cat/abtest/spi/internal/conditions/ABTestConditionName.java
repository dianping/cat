package com.dianping.cat.abtest.spi.internal.conditions;

public enum ABTestConditionName {

	URL("url"),
	
	CITY("city"),
	
	VISITOR_TYPE("vistor_type"),

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
