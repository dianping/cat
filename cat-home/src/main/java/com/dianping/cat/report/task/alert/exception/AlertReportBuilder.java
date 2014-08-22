package com.dianping.cat.report.task.alert.exception;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.alert.report.transform.DefaultNativeBuilder;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class AlertReportBuilder implements ReportTaskBuilder {

	public static final String ID = Constants.REPORT_ALERT;

	@Inject
	protected ReportServiceManager m_reportService;

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		AlertReport alertReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);
		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		AlertReport alertReport = new AlertReport(Constants.CAT);
		TopReportVisitor visitor = new TopReportVisitor().setReport(alertReport).setExceptionConfigManager(
		      m_exceptionConfigManager);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);

		alertReport.setStartTime(start);
		alertReport.setEndTime(end);

		TopReport topReport = m_reportService.queryTopReport(Constants.CAT, start, end);
		visitor.visitTopReport(topReport);

		HourlyReport report = new HourlyReport();
		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);

		return m_reportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		AlertReport alertReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);

		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		AlertReport alertReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);

		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private AlertReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		AlertReportMerger merger = new AlertReportMerger(new AlertReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				AlertReport reportModel = m_reportService.queryAlertReport(domain, new Date(startTime), new Date(startTime
				      + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		AlertReport alertReport = merger.getAlertReport();
		alertReport.setStartTime(start);
		alertReport.setEndTime(end);
		return alertReport;
	}

	private AlertReport queryHourlyReportsByDuration(String name, String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		AlertReportMerger merger = new AlertReportMerger(new AlertReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			AlertReport reportModel = m_reportService.queryAlertReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));

			reportModel.accept(merger);
		}
		AlertReport alertReport = merger.getAlertReport();

		return alertReport;
	}
}
