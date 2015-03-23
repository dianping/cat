package com.dianping.cat.report.service;

import java.util.Date;
import java.util.Set;

import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;

public interface ReportService<T> {
	public boolean insertDailyReport(DailyReport report, byte[] content);

	public boolean insertHourlyReport(HourlyReport report, byte[] content);

	public boolean insertMonthlyReport(MonthlyReport report, byte[] content);

	public boolean insertWeeklyReport(WeeklyReport report, byte[] content);

	public Set<String> queryAllDomainNames(Date start, Date end, String name);
	
	public T queryDailyReport(String domain, Date start, Date end);

	public T queryHourlyReport(String domain, Date start, Date end);

	public T queryMonthlyReport(String domain, Date start);

	public T queryWeeklyReport(String domain, Date start);

	public T queryReport(String domain, Date start, Date end);

}