package com.dianping.cat.server;


public enum MetricType {

	AVG("mean", "平均值"),

	SUM("sum", "总和"),

	MAX("max", "最大值"),

	MIN("min", "最小值"),

	COUNT("count", "个数");

	private String m_name;

	private String m_title;

	public static MetricType getByName(String name, MetricType defaultType) {
		for (MetricType type : values()) {
			if (type.getName().equals(name)) {
				return type;
			}
		}

		return defaultType;
	}

	private MetricType(String name, String title) {
		m_name = name;
		m_title = title;
	}

	public String getName() {
		return m_name;
	}

	public String getTitle() {
		return m_title;
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setTitle(String title) {
		m_title = title;
	}

}
