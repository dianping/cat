package com.dianping.cat.consumer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;

public abstract class MockReportManager<T> implements ReportManager<T> {

	private Set<String> m_set;

	@Override
	public void initialize() {
	}

	@Override
	public Set<String> getDomains(long startTime) {
		if (m_set == null) {
			m_set = new HashSet<String>();

			m_set.add("group");
		}

		return m_set;
	}

	@Override
	public abstract T getHourlyReport(long startTime, String domain, boolean createIfNotExist);

	@Override
	public Map<String, T> getHourlyReports(long startTime) {
		return null;
	}

	@Override
	public Map<String, T> loadHourlyReports(long startTime, StoragePolicy policy, int index) {
		return null;
	}

	@Override
	public void storeHourlyReports(long startTime, StoragePolicy policy, int index) {
	}

}
