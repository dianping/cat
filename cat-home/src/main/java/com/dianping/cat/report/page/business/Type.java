package com.dianping.cat.report.page.business;

public enum Type {
	Domain("domain"),

	Tag("tag");

	private String m_name;

	private Type(String type) {
		m_name = type;
	}

	public static Type getType(String str, Type defaultType) {
		for (Type type : Type.values()) {
			if (type.getName().equalsIgnoreCase(str)) {
				return type;
			}
		}

		return defaultType;
	}

	public String getName() {
		return m_name;
	}
}
