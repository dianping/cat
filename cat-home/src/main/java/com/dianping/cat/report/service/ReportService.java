package com.dianping.cat.report.service;

import java.util.Date;
import java.util.Set;

public interface ReportService<T> {

	public Set<String> queryAllDomainNames(Date start, Date end, String name);
	
	public T queryDailyReport(String domain, Date start, Date end);

	public T queryHourlyReport(String domain, Date start, Date end);

	public T queryMonthlyReport(String domain, Date start);

	public T queryWeeklyReport(String domain, Date start);

	public T queryReport(String domain, Date start, Date end);

}