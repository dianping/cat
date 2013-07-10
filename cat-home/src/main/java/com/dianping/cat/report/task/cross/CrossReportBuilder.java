package com.dianping.cat.report.task.cross;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class CrossReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		CrossReport crossReport = queryHourlyReportsByDuration(name, domain, period,TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setContent(crossReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertDailyReport(report);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Cross report don't support HourReport!");
	}

	private CrossReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				CrossReport reportModel = m_reportService.queryCrossReport(domain, new Date(startTime), new Date(startTime
				      + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		CrossReport crossReport = merger.getCrossReport();
		crossReport.setStartTime(start);
		crossReport.setEndTime(end);
		return crossReport;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		CrossReport crossReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent(crossReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		return m_reportService.insertMonthlyReport(report);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		Date start = period;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY * 7);

		CrossReport crossReport = queryDailyReportsByDuration(domain, start, end);
		WeeklyReport report = new WeeklyReport();
		String content = crossReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		return m_reportService.insertWeeklyReport(report);
	}

	private CrossReport queryHourlyReportsByDuration(String name, String domain, Date period,Date endDate) {
		Set<String> domainSet = m_reportService.queryAllDomainNames(period, endDate, "cross");
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			CrossReport reportModel = m_reportService.queryCrossReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));
			
			reportModel.accept(merger);
		}
		CrossReport crossReport = merger.getCrossReport();
		crossReport.getDomainNames().addAll(domainSet);
		crossReport.setStartTime(period);
		crossReport.setEndTime(endDate);

		return crossReport;
	}
}
