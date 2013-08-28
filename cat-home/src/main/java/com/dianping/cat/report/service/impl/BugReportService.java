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
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.service.AbstractReportService;
import com.dianping.cat.report.task.bug.BugReportMerger;
import com.dianping.cat.service.ReportConstants;

public class BugReportService extends AbstractReportService<BugReport> {

	@Inject
	private DailyReportDao m_dailyReportDao;

	@Inject
	private WeeklyReportDao m_weeklyReportDao;

	@Inject
	private MonthlyReportDao m_monthlyReportDao;

	@Override
	public BugReport makeReport(String domain, Date start, Date end) {
		BugReport report = new BugReport(domain);

		report.setStartTime(start);
		report.setEndTime(end);
		return report;
	}

	@Override
	public BugReport queryDailyReport(String domain, Date start, Date end) {
		BugReportMerger merger = new BugReportMerger(new BugReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = ReportConstants.REPORT_BUG;

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_DAY) {
			try {
				DailyReport report = m_dailyReportDao.findByDomainNamePeriod(domain, name, new Date(startTime),
				      DailyReportEntity.READSET_FULL);
				String xml = report.getContent();
				BugReport reportModel = com.dianping.cat.home.bug.transform.DefaultSaxParser.parse(xml);
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		BugReport bugReport = merger.getBugReport();

		bugReport.setStartTime(start);
		bugReport.setEndTime(end);
		return bugReport;
	}

	@Override
	public BugReport queryHourlyReport(String domain, Date start, Date end) {
		BugReportMerger merger = new BugReportMerger(new BugReport(domain));
		long startTime = start.getTime();
		long endTime = end.getTime();
		String name = ReportConstants.REPORT_BUG;

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
						BugReport reportModel = com.dianping.cat.home.bug.transform.DefaultSaxParser.parse(xml);
						reportModel.accept(merger);
					} catch (Exception e) {
						Cat.logError(e);
						Cat.getProducer().logEvent("ErrorXML", name, Event.SUCCESS,
						      report.getDomain() + " " + report.getPeriod() + " " + report.getId());
					}
				}
			}
		}
		BugReport bugReport = merger.getBugReport();

		bugReport.setStartTime(start);
		bugReport.setEndTime(new Date(end.getTime() - 1));

		return bugReport;
	}

	@Override
	public BugReport queryMonthlyReport(String domain, Date start) {
		try {
			MonthlyReport entity = m_monthlyReportDao.findReportByDomainNamePeriod(start, domain,
			      ReportConstants.REPORT_BUG, MonthlyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.home.bug.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new BugReport(domain);
	}

	@Override
	public BugReport queryWeeklyReport(String domain, Date start) {
		try {
			WeeklyReport entity = m_weeklyReportDao.findReportByDomainNamePeriod(start, domain,
			      ReportConstants.REPORT_BUG, WeeklyReportEntity.READSET_FULL);
			String content = entity.getContent();

			return com.dianping.cat.home.bug.transform.DefaultSaxParser.parse(content);
		} catch (Exception e) {
			Cat.logError(e);
		}
		return new BugReport(domain);
	}

}
