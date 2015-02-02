package com.dianping.cat.report.page.metric;

public enum Range {
	ONE("1小时", 1),

	TWO("2小时", 2),

	FOUR("4小时", 4),

	SIX("6小时", 6),

	EIGHT("8小时", 8),

	TWELVE("12小时", 12),

	ONE_DAY("24小时", 24),

	TWO_DAY("48小时", 48), ;

	private String m_title;

	private int m_duration;

	public static Range getByTitle(String title, Range defaultRange) {
		for (Range range : Range.values()) {
			if (range.getTitle().equals(title)) {
				return range;
			}
		}
		return defaultRange;
	}

	private Range(String title, int duration) {
		m_title = title;
		m_duration = duration;
	}

	public int getDuration() {
		return m_duration;
	}

	public String getTitle() {
		return m_title;
	}
}
