package com.dianping.cat.report.app;

import java.util.Date;

import com.dianping.cat.app.AppDailyReport;

public interface AppReportService<T> {

	public boolean insertDailyReport(AppDailyReport report, byte[] content);

	public T queryDailyReport(int namespace, Date start, Date end);

}