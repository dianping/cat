package com.dianping.cat.report.page.crash.display;

public enum SortType {

	COUNT("count"),

	DAU("dau"),

	PERCENT("percent"),

	VERSION("version"),

	COUNT_MOM("countMoM"),

	COUNT_YOY("countYoY"),

	PERCENT_MOM("percentMoM"),

	PERCENT_YOY("percentYoY");

	String m_name;

	private SortType(String name) {
		m_name = name;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		m_name = name;
	}

	public static SortType findByName(String name) {
		for (SortType sortType : SortType.values()) {
			if (sortType.getName().equals(name)) {
				return sortType;
			}
		}

		throw new RuntimeException("Wrong Sort Type.");
	}
}
