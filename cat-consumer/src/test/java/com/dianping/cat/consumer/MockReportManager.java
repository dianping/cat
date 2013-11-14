package com.dianping.cat.consumer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.service.ReportManager;

public class MockReportManager<T> implements ReportManager<T> {

	@Inject
	private ReportDelegate<T> m_delegate;

	private T m_report;

	private Set<String> m_set;

	@Override
	public void cleanup() {
	}

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
	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist) {
		if (m_report == null) {
			m_report = m_delegate.makeReport(domain, startTime, Constants.HOUR);
		}
		return m_report;
	}

	@Override
	public Map<String, T> getHourlyReports(long startTime) {
		return null;
	}

	@Override
	public Map<String, T> loadHourlyReports(long startTime, StoragePolicy policy) {
		return null;
	}

	@Override
	public void storeHourlyReports(long startTime, StoragePolicy policy) {
	}

}
