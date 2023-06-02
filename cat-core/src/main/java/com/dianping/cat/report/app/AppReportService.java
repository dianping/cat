package com.dianping.cat.report.app;

import com.dianping.cat.app.AppDailyReport;

import java.util.Date;

public interface AppReportService<T> {

	public boolean insertDailyReport(AppDailyReport report, byte[] content);

	public T queryDailyReport(int namespace, Date start, Date end);

}
