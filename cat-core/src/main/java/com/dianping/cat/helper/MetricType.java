package com.dianping.cat.helper;

public enum MetricType {
	COUNT("COUNT", "(次数)"),

	AVG("AVG", "(平均)"),

	SUM("SUM", "(总和)");

	private String m_name;

	private String m_desc;

	MetricType(String name, String desc) {
		m_name = name;
		m_desc = desc;
	}

	public String getName() {
		return m_name;
	}

	public String getDesc() {
		return m_desc;
	}

	public static MetricType getTypeByName(String name) {
		for (MetricType type : MetricType.values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}
		throw new RuntimeException("Unsupported MetricType Name!");
	}

	public static String getDesByName(String name) {
		for (MetricType type : MetricType.values()) {
			if (type.getName().equals(name)) {
				return type.getDesc();
			}
		}
		throw new RuntimeException("Unsupported MetricType Name!");
	}
};
