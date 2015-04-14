package com.dianping.cat.report.page.statistics.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportContent;
import com.dianping.cat.core.dal.HourlyReportContentEntity;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.core.dal.DailyReportContent;
import com.dianping.cat.core.dal.DailyReportContentEntity;
import com.dianping.cat.core.dal.MonthlyReportContent;
import com.dianping.cat.core.dal.MonthlyReportContentEntity;
import com.dianping.cat.core.dal.WeeklyReportContent;
import com.dianping.cat.core.dal.WeeklyReportContentEntity;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.transform.DefaultNativeParser;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportMerger;
import com.dianping.cat.report.service.AbstractReportService;

public class HeavyReportService extends AbstractReportService<HeavyReport> {

	@Override
	public HeavyReport makeReport(String domain, Date start, Date end) {
		HeavyReport report = new HeavyReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public HeavyReport queryDailyReport(String domain, Date start, Date end) {
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_HEAVY;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				HeavyReport reportModel = queryFromDailyBinary(report.getId(), domain);

				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		HeavyReport heavyReport = merger.getHeavyReport();

		heavyReport.setStartTime(start);
		heavyReport.setEndTime(end);
		return heavyReport;
	}

	private HeavyReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new HeavyReport(domain);
		}
	}

	private HeavyReport queryFromHourlyBinary(int id, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, HourlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new HeavyReport(domain);
		}
	}

	private HeavyReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new HeavyReport(domain);
		}
	}

	private HeavyReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new HeavyReport(domain);
		}
	}

	@Override
	public HeavyReport queryHourlyReport(String domain, Date start, Date end) {
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_HEAVY;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
				      HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					try {
						HeavyReport reportModel = queryFromHourlyBinary(report.getId(), domain);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		HeavyReport heavyReport = merger.getHeavyReport();

		heavyReport.setStartTime(start);
		heavyReport.setEndTime(new Date(end.getTime() - 1));

		return heavyReport;
	}

	@Override
	public HeavyReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, Constants.REPORT_HEAVY,
			      MonthlyReportEntity.READSET_FULL);
			return queryFromMonthlyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HeavyReport(domain);
	}

	@Override
	public HeavyReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain, Constants.REPORT_HEAVY,
			      WeeklyReportEntity.READSET_FULL);
			return queryFromWeeklyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new HeavyReport(domain);
	}

}
