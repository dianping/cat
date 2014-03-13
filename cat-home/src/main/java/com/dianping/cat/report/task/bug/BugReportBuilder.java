package com.dianping.cat.report.task.bug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.entity.Domain;
import com.dianping.cat.home.bug.transform.DefaultNativeBuilder;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class BugReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	private SimpleDateFormat m_hourly_formate = new SimpleDateFormat("yyyyMMddHH");

	private SimpleDateFormat m_daily_formate = new SimpleDateFormat("yyyyMMdd");

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		BugReport bugReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));

		for (Domain d : bugReport.getDomains().values()) {
			d.setProblemUrl(String.format("http://%s/cat/r/p?op=history&reportType=day&domain=%s&date=%s",
			      getDomainName(), d.getId(), m_daily_formate.format(period)));
		}
		DailyReport report = new DailyReport();

		report.setContent(bugReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(bugReport);
		return m_reportService.insertDailyReport(report, binaryContent);
	}

	private boolean validateDomain(String domain) {
		return !domain.equals(Constants.FRONT_END) && !domain.equals(Constants.ALL);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		BugReport bugReport = new BugReport(Constants.CAT);
		ProblemReportVisitor visitor = new ProblemReportVisitor().setReport(bugReport);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, ProblemAnalyzer.ID);

		for (String domainName : domains) {
			if (validateDomain(domainName)) {
				ProblemReport problemReport = m_reportService.queryProblemReport(domainName, start, end);
				visitor.visitProblemReport(problemReport);
			}
		}

		for (Domain d : bugReport.getDomains().values()) {
			d.setProblemUrl(String.format("http://%s/cat/r/p?domain=%s&date=%s", getDomainName(), d.getId(),
			      m_hourly_formate.format(start)));
		}
		HourlyReport report = new HourlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(bugReport);
		return m_reportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		BugReport bugReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));

		for (Domain d : bugReport.getDomains().values()) {
			d.setProblemUrl(String.format("http://%s/cat/r/p?op=history&reportType=month&domain=%s&date=%s",
			      getDomainName(), d.getId(), m_daily_formate.format(period)));
		}
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(bugReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		BugReport bugReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime() + TimeUtil.ONE_WEEK));

		for (Domain d : bugReport.getDomains().values()) {
			d.setProblemUrl(String.format("http://%s/cat/r/p?op=history&reportType=week&domain=%s&date=%s",
			      getDomainName(), d.getId(), m_daily_formate.format(period)));
		}
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(bugReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private BugReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		HistoryBugReportMerger merger = new HistoryBugReportMerger(new BugReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				BugReport reportModel = m_reportService.queryBugReport(domain, new Date(startTime), new Date(startTime
				      + TimeUtil.ONE_DAY));
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

	private BugReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		BugReportMerger merger = new BugReportMerger(new BugReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			BugReport reportModel = m_reportService.queryBugReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));

			reportModel.accept(merger);
		}
		com.dianping.cat.home.bug.entity.BugReport bugReport = merger.getBugReport();

		return bugReport;
	}

	private String getDomainName() {
		String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		return ip + ":8080";
	}
}
