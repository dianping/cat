package com.dianping.cat.report;

import java.util.Date;

public interface ReportService<T> {
	public T parseReport(String name, String xml) throws Exception;

	public T createReport(String name, String domain, long startTime, long duration);

	public T getHouylyReport(String name, String domain, long startTime);

	public T getDailyReport(String name, String domain, Date start);

	public T getWeeklyReport(String name, String domain, Date start);

	public T getMonthlyReport(String name, String domain, Date start);

	public T getDailyReportByPeriod(String name, String domain, Date start, Date end);
}
