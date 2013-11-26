package com.dianping.cat.report.task.sql;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.consumer.sql.SqlReportMerger;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class SqlReportBuilder implements ReportTaskBuilder {

	@Inject
	private SqlMerger m_sqlMerger;

	@Inject
	protected ReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		SqlReport sqlReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(sqlReport);
		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Sql report don't support HourReport!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		SqlReport sqlReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(sqlReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		SqlReport sqlReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime() + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(sqlReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private SqlReport queryDailyReportsByDuration(String domain, Date start, Date end) {
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

	private SqlReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end) {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, end, SqlAnalyzer.ID);
		long startTime = start.getTime();
		long endTime = end.getTime();
		List<SqlReport> reports = new ArrayList<SqlReport>();

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			SqlReport reportModel = m_reportService.querySqlReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));

			reports.add(reportModel);
		}
		SqlReport sqlReport = m_sqlMerger.mergeForDaily(domain, reports, domainSet);

		sqlReport.setStartTime(start);
		sqlReport.setEndTime(end);
		return sqlReport;
	}

}
