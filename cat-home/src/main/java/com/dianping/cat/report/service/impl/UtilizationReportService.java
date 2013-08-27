package com.dianping.cat.report.service.impl;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.DailyReportDao;
import com.dianping.cat.core.dal.DailyReportEntity;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportEntity;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.MonthlyReportDao;
import com.dianping.cat.core.dal.MonthlyReportEntity;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.core.dal.WeeklyReportDao;
import com.dianping.cat.core.dal.WeeklyReportEntity;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.service.AbstractReportService;
import com.dianping.cat.report.task.utilization.UtilizationReportMerger;

public class UtilizationReportService extends AbstractReportService<UtilizationReport> {

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

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
		String name = "utilization";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				
				UtilizationReport reportModel = com.dianping.cat.home.utilization.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);
		return utilizationReport;
	}

	@Override
	public UtilizationReport queryHourlyReport(String domain, Date start, Date end) {
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = "utilization";

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			List<HourlyReport> reports = null;
			try {
				reports = m_hourlyReportDao.findAllByDomainNamePeriod(new Date(startTime), domain, name,
				      HourlyReportEntity.READSET_FULL);
			} catch (DalException e) {
				Cat.logError(e);
			}
			if (reports != null) {
				for (HourlyReport report : reports) {
					String xml = report.getContent();

					try {
						UtilizationReport reportModel = com.dianping.cat.home.utilization.transform.DefaultSaxParser.parse(xml);
						reportModel.accept(merger);
					} catch (Exception e) {
						Cat.logError(e);
						Cat.getProducer().logEvent("ErrorXML", name, Event.SUCCESS,
						      report.getDomain() + " " + report.getPeriod() + " " + report.getId());
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
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain, "utilization",
			      MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.home.utilization.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new UtilizationReport(domain);
	}

	@Override
	public UtilizationReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain, "utilization",
			      WeeklyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.home.utilization.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new UtilizationReport(domain);
	}

}
