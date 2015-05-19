package com.dianping.cat.report;

import java.util.Map;
import java.util.Set;

import com.dianping.cat.report.DefaultReportManager.StoragePolicy;

public interface ReportManager<T> {

	public void destory();

	public void initialize();

	public Set<String> getDomains(long startTime);

	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist);

	public Map<String, T> getHourlyReports(long startTime);

	public Map<String, T> loadHourlyReports(long startTime, StoragePolicy policy, int index);

	public void storeHourlyReports(long startTime, StoragePolicy policy, int index);

}
