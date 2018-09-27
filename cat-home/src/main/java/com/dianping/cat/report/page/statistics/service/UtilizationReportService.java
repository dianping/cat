package com.dianping.cat.report.page.statistics.service;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Named;

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
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultNativeParser;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportMerger;
import com.dianping.cat.report.service.AbstractReportService;

@Named
public class UtilizationReportService extends AbstractReportService<UtilizationReport> {

	@Override
	public UtilizationReport makeReport(String domain, Date start, Date end) {
		UtilizationReport report = new UtilizationReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public UtilizationReport queryDailyReport(String domain, Date start, Date end) {
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_UTILIZATION;

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				UtilizationReport reportModel = queryFromDailyBinary(report.getId(), domain);
				reportModel.accept(merger);
			} catch (DalNotFoundException e) {
				// ignore
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);
		return utilizationReport;
	}

	private UtilizationReport queryFromDailyBinary(int id, String domain) throws DalException {
		DailyReportContent content = m_dailyReportContentDao.findByPK(id, DailyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	private UtilizationReport queryFromHourlyBinary(int id, Date period, String domain) throws DalException {
		HourlyReportContent content = m_hourlyReportContentDao.findByPK(id, period,
		      HourlyReportContentEntity.READSET_CONTENT);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	private UtilizationReport queryFromMonthlyBinary(int id, String domain) throws DalException {
		MonthlyReportContent content = m_monthlyReportContentDao.findByPK(id, MonthlyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	private UtilizationReport queryFromWeeklyBinary(int id, String domain) throws DalException {
		WeeklyReportContent content = m_weeklyReportContentDao.findByPK(id, WeeklyReportContentEntity.READSET_FULL);

		if (content != null) {
			return DefaultNativeParser.parse(content.getContent());
		} else {
			return new UtilizationReport(domain);
		}
	}

	@Override
	public UtilizationReport queryHourlyReport(String domain, Date start, Date end) {
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = Constants.REPORT_UTILIZATION;

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
						UtilizationReport reportModel = queryFromHourlyBinary(report.getId(), report.getPeriod(), domain);
						reportModel.accept(merger);
					} catch (DalNotFoundException e) {
						// ignore
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(new Date(end.getTime() - 1));

		return utilizationReport;
	}

	@Override
	public UtilizationReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain,
			      Constants.REPORT_UTILIZATION, MonthlyReportEntity.READSET_FULL);
			return queryFromMonthlyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new UtilizationReport(domain);
	}

	@Override
	public UtilizationReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain,
			      Constants.REPORT_UTILIZATION, WeeklyReportEntity.READSET_FULL);
			return queryFromWeeklyBinary(entity.getId(), domain);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new UtilizationReport(domain);
	}

}
