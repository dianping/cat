package com.dianping.cat.report.service;

import java.util.Date;

public abstract class AbstractReportService<T> {

	public static final int s_hourly = 1;

	public static final int s_daily = 2;

	public static final int s_historyDaily = 3;

	public static final int s_currentWeekly = 4;

	public static final int s_historyWeekly = 5;

	public static final int s_currentMonth = 6;

	public static final int s_historyMonth = 7;

	public static final int s_customer = 8;

	public abstract T queryHourlyReport(String domain, Date start, Date end);

	public abstract T queryDailyReport(String domain, Date start, Date end);

	public abstract T queryWeeklyReport(String domain, Date start, Date end);

	public abstract T queryMonthlyReport(String domain, Date start, Date end);

	public abstract T queryCurrentWeeklyReport(String domain, Date start, Date end);

	public abstract T queryCurrentMonthlyReport(String domain, Date start, Date end);

	public T queryReport(String domain, Date start, Date end, int type) {
		if (type == s_hourly) {
			return queryHourlyReport(domain, start, end);
		} else if (type == s_daily) {
			return queryDailyReport(domain, start, end);
		} else if (type == s_historyDaily) {
			return queryDailyReport(domain, start, end);
		} else if (type == s_historyWeekly) {
			return queryWeeklyReport(domain, start, end);
		} else if (type == s_historyMonth) {
			return queryMonthlyReport(domain, start, end);
		} else if (type == s_currentWeekly) {
			return queryCurrentWeeklyReport(domain, start, end);
		} else if (type == s_currentMonth) {
			return queryCurrentMonthlyReport(domain, start, end);
		} else {
			return queryDailyReport(domain, start, end);
		}
	}
	
}
