package com.dianping.cat.report.task.sql;

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.sql.SqlReportMerger;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class SqlReportBuilder implements ReportBuilder {

	@Inject
	protected ReportService m_reportService;
	
	@Override
	public boolean buildDailyReport(String name, String domain, Date period) {
		DailyReport report = queryDailyReport(name, domain, period);
		return m_reportService.insertDailyReport(report);
	}

	@Override
	public boolean buildHourReport(String name, String domain, Date period) {
		throw new RuntimeException("Sql report don't support HourReport!");
	}

	private SqlReport buildMergedDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				SqlReport reportModel = m_reportService.querySqlReport(domain, new Date(startTime), new Date(startTime
				      + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		SqlReport sqlReport = merger.getSqlReport();
		sqlReport.setStartTime(start);
		sqlReport.setEndTime(end);
		return sqlReport;
	}

	@Override
	public boolean buildMonthReport(String name, String domain, Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.MONTH, 1);

		Date start = period;
		Date end = cal.getTime();

		SqlReport sqlReport = buildMergedDailyReport(domain, start, end);
		MonthlyReport report = new MonthlyReport();

		report.setContent(sqlReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		
		return m_reportService.insertMonthlyReport(report);
	}

	@Override
	public boolean buildWeeklyReport(String name, String domain, Date period) {
		Date start = period;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY * 7);

		SqlReport sqlReport = buildMergedDailyReport(domain, start, end);
		WeeklyReport report = new WeeklyReport();
		String content = sqlReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		
		return m_reportService.insertWeeklyReport(report);
	}

	private DailyReport queryDailyReport(String name, String domain, Date period) {
		Date endDate = TaskHelper.tomorrowZero(period);
		Set<String> domainSet = m_reportService.queryAllDomainNames(period, endDate, "sql");
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(domain));
		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);

			SqlReport reportModel = m_reportService.querySqlReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));
			reportModel.accept(merger);

		}
		SqlReport sqlReport = merger.getSqlReport();
		sqlReport.getDomainNames().addAll(domainSet);
		sqlReport.setStartTime(period);
		sqlReport.setEndTime(endDate);
		
		String content = sqlReport.toString();
		DailyReport report = new DailyReport();
		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return report;
	}

}
