package com.dianping.cat.report.task.alert;

import java.util.HashMap;
import java.util.Map;

public enum AlertType {

	Business("business"), Network("network"), System("system"), 
	ThirdParty("thirdParty"), Exception("exception"), FrontEndException("FRONT_END_EXCEPTION");

	private String m_name;

	static Map<String, AlertType> m_alertTypes = new HashMap<String, AlertType>();

	AlertType(String name) {
		m_name = name;
	}

	static {
		for (AlertType type : AlertType.values()) {
			m_alertTypes.put(type.getName(), type);
		}
	}

	public String getName() {
		return m_name;
	}

	public static AlertType getTypeByName(String name) {
		return m_alertTypes.get(name);
	}

}
