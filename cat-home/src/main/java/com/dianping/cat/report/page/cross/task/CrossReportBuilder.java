package com.dianping.cat.report.page.cross.task;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossReportMerger;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class CrossReportBuilder implements TaskBuilder {
	
	public static final String ID = CrossAnalyzer.ID;

	@Inject
	protected CrossReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		CrossReport crossReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(crossReport);
		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Cross report don't support HourlyReport!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		CrossReport crossReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(crossReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		CrossReport crossReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeHelper.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(crossReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private CrossReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				CrossReport reportModel = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
				      + TimeHelper.ONE_DAY));
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

	private CrossReport queryHourlyReportsByDuration(String name, String domain, Date period, Date endDate) {
		Set<String> domainSet = m_reportService.queryAllDomainNames(period, endDate, CrossAnalyzer.ID);
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			CrossReport reportModel = m_reportService.queryReport(domain, date, new Date(date.getTime()
			      + TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		CrossReport crossReport = merger.getCrossReport();
		crossReport.getDomainNames().addAll(domainSet);
		crossReport.setStartTime(period);
		crossReport.setEndTime(endDate);

		return crossReport;
	}
}
