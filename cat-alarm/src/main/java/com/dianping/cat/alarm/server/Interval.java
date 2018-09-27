package com.dianping.cat.alarm.server;

import com.dianping.cat.helper.TimeHelper;

public enum Interval {

	SECOND("s", TimeHelper.ONE_SECOND),

	MINUTE("m", TimeHelper.ONE_MINUTE),

	HOUR("h", TimeHelper.ONE_HOUR),

	DAY("d", TimeHelper.ONE_DAY),

	WEEK("w", TimeHelper.ONE_WEEK);

	private String m_name;

	private long m_time;

	private Interval(String name, long time) {
		m_name = name;
		m_time = time;
	}

	public String getName() {
		return m_name;
	}

	public long getTime() {
		return m_time;
	}

	public static Interval findByName(String name, Interval defaultValue) {
		for (Interval interval : values()) {
			if (interval.getName().equalsIgnoreCase(name)) {
				return interval;
			}
		}
		return defaultValue;
	}

	public static Interval findByInterval(String interval) {
		for (Interval intval : values()) {
			if (interval.endsWith(intval.getName())) {
				return intval;
			}
		}
		return null;
	}

}
