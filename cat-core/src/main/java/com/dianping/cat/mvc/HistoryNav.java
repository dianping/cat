package com.dianping.cat.mvc;

public enum HistoryNav {

	MONTH("month", "-1m", "+1m"),

	WEEK("week", "-1w", "+1w"),

	DAY("day", "-1d", "+1d");

	private String m_last;

	private String m_next;

	private String m_title;

	public static HistoryNav getByName(String name) {
		for (HistoryNav nav : HistoryNav.values()) {
			if (nav.getTitle().equalsIgnoreCase(name)) {
				return nav;
			}
		}
		return HistoryNav.DAY;
	}

	private HistoryNav(String name, String last, String next) {
		m_title = name;
		m_last = last;
		m_next = next;
	}

	public String getLast() {
		return m_last;
	}

	public String getNext() {
		return m_next;
	}

	public String getTitle() {
		return m_title;
	}
}
