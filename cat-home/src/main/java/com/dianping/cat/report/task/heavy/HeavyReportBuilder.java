package com.dianping.cat.report.task.heavy;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;
import com.dianping.cat.service.ReportConstants;

public class HeavyReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		HeavyReport heavyReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setContent(heavyReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertDailyReport(report);
	}

	private boolean validateDomain(String domain) {
		return !domain.equals(ReportConstants.FRONT_END) && !domain.equals(ReportConstants.ALL);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		HeavyReport heavyReport = new HeavyReport(ReportConstants.CAT);
		MatrixReportVisitor visitor = new MatrixReportVisitor().setReport(heavyReport);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, MatrixAnalyzer.ID);

		heavyReport.setStartTime(start);
		heavyReport.setEndTime(end);
		for (String domainName : domains) {
			if (validateDomain(domainName)) {
				MatrixReport matrixReport = m_reportService.queryMatrixReport(domainName, start, end);

				visitor.visitMatrixReport(matrixReport);
			}
		}

		HourlyReport report = new HourlyReport();

		report.setContent(heavyReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);
		return m_reportService.insertHourlyReport(report);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		HeavyReport heavyReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent(heavyReport.toString());
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
		HeavyReport heavyReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();
		String content = heavyReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertWeeklyReport(report);
	}

	private HeavyReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				HeavyReport reportModel = m_reportService.queryHeavyReport(domain, new Date(startTime), new Date(startTime
				      + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		HeavyReport heavyReport = merger.getHeavyReport();
		heavyReport.setStartTime(start);
		heavyReport.setEndTime(end);
		return heavyReport;
	}

	private HeavyReport queryHourlyReportsByDuration(String name, String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			HeavyReport reportModel = m_reportService.queryHeavyReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));

			reportModel.accept(merger);
		}
		com.dianping.cat.home.heavy.entity.HeavyReport heavyReport = merger.getHeavyReport();

		return heavyReport;
	}

}
