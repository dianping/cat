package com.dianping.cat.report;

import com.dianping.cat.report.DefaultReportManager.FlushPolicy;

public interface ReportManager<T> {
	public T getHourlyReport(long startTime, String domain, boolean createIfNotExist);

	public void storeReports(long startTime, FlushPolicy policy);
}
