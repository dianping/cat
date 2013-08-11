package com.dianping.cat.report.task.bug;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.entity.Domain;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class BugReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		BugReport bugReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setContent(bugReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertDailyReport(report);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		BugReport bugReport = new BugReport(CatString.CAT);
		ProblemReportVisitor visitor = new ProblemReportVisitor().setReport(bugReport);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, "problem");

		for (String domainName : domains) {
			ProblemReport problemReport = m_reportService.queryProblemReport(domainName, start, end);
			visitor.visitProblemReport(problemReport);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHH");
		for (Domain d : bugReport.getDomains().values()) {
			d.setProblemUrl(String.format("http://%s/cat/r/p?domain=%s&date=%s", getDomainName(), d.getId(), sdf.format(start)));
		}
		HourlyReport report = new HourlyReport();

		report.setContent(bugReport.toString());
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
		BugReport bugReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent(bugReport.toString());
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
		BugReport bugReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime() + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();
		String content = bugReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertWeeklyReport(report);
	}

	private BugReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		BugReportMerger merger = new BugReportMerger(new BugReport(domain));

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

	private BugReport queryHourlyReportsByDuration(String name, String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
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

		if ("10.1.6.128".equals(ip)) {
			return "cat.dianpingoa.com";
		} else if ("192.168.7.70".equals(ip)) {
			return "cat.qa.dianpingoa.com";
		} else {
			return ip + ":2281";
		}
	}
}
