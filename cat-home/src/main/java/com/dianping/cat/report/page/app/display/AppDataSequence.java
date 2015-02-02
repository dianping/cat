package com.dianping.cat.report.page.app.display;

import java.util.List;
import java.util.Map;

public class AppDataSequence<T> {

	protected int m_duration;

	protected Map<Integer, List<T>> m_records;

	public AppDataSequence(int duration, Map<Integer, List<T>> reocords) {
		m_duration = duration;
		m_records = reocords;
	}

	public Map<Integer, List<T>> getRecords() {
		return m_records;
	}

	public int getDuration() {
		return m_duration;
	}
}
