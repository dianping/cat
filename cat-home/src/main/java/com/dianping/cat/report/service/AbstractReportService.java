package com.dianping.cat.report.service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.DailyReportContentDao;
import com.dianping.cat.home.dal.report.MonthlyReportContentDao;
import com.dianping.cat.home.dal.report.WeeklyReportContentDao;

public abstract class AbstractReportService<T> {

	@Inject
	protected HourlyReportDao m_hourlyReportDao;

	@Inject
	protected HourlyReportContentDao m_hourlyReportContentDao;

	@Inject
	protected DailyReportDao m_dailyReportDao;

	@Inject
	protected DailyReportContentDao m_dailyReportContentDao;

	@Inject
	protected WeeklyReportDao m_weeklyReportDao;

	@Inject
	protected WeeklyReportContentDao m_weeklyReportContentDao;

	@Inject
	protected MonthlyReportDao m_monthlyReportDao;

	@Inject
	protected MonthlyReportContentDao m_monthlyReportContentDao;

	public static final int s_hourly = 1;

	public static final int s_daily = 2;

	public static final int s_weekly = 3;

	public static final int s_monthly = 4;

	public static final int s_customer = 5;

	public T queryReport(String domain, Date start, Date end) {
		int type = computeQueryType(start, end);
		T report = null;

		if (type == s_hourly) {
			report = queryHourlyReport(domain, start, end);
		} else if (type == s_daily) {
			report = queryDailyReport(domain, start, end);
		} else if (type == s_weekly) {
			report = queryWeeklyReport(domain, start);
		} else if (type == s_monthly) {
			report = queryMonthlyReport(domain, start);
		} else {
			report = queryDailyReport(domain, start, end);
		}
		if (report == null) {
			report = makeReport(domain, start, end);
		}
		return report;
	}

	public Set<String> queryAllDomainNames(Date start, Date end, String name) {
		HashSet<String> domains = new HashSet<String>();
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			domains.addAll(queryAllDomains(new Date(startTime)));
		}
		return domains;
	}

	private Set<String> queryAllDomains(Date start) {
		Set<String> domains = new HashSet<String>();
		try {
			List<HourlyReport> reports = m_hourlyReportDao.findAllByPeriod(start, HourlyReportEntity.READSET_DOMAIN_NAME);

			if (reports != null) {
				for (HourlyReport report : reports) {
					domains.add(report.getDomain());
				}
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
		return domains;
	}

	public abstract T makeReport(String domain, Date start, Date end);

	public abstract T queryHourlyReport(String domain, Date start, Date end);

	public abstract T queryDailyReport(String domain, Date start, Date end);

	public abstract T queryWeeklyReport(String domain, Date start);

	public abstract T queryMonthlyReport(String domain, Date start);

	public int computeQueryType(Date start, Date end) {
		long duration = end.getTime() - start.getTime();

		if (duration == TimeUtil.ONE_HOUR) {
			return s_hourly;
		}
		if (duration == TimeUtil.ONE_DAY) {
			return s_daily;
		}
		Calendar startCal = Calendar.getInstance();
		startCal.setTime(start);

		if (duration == TimeUtil.ONE_WEEK && startCal.get(Calendar.DAY_OF_WEEK) == 7) {
			return s_weekly;
		}
		Calendar endCal = Calendar.getInstance();
		endCal.setTime(end);

		if (startCal.get(Calendar.DAY_OF_MONTH) == 1 && endCal.get(Calendar.DAY_OF_MONTH) == 1) {
			return s_monthly;
		}
		return s_customer;
	}
}
