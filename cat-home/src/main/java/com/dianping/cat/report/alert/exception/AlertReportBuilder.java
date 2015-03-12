package com.dianping.cat.report.alert.exception;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.alert.report.transform.DefaultNativeBuilder;
import com.dianping.cat.report.service.impl.AlertReportService;
import com.dianping.cat.report.service.impl.TopReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.system.config.ExceptionRuleConfigManager;

public class AlertReportBuilder implements TaskBuilder {

	public static final String ID = Constants.REPORT_ALERT;

	@Inject
	protected AlertReportService m_alertReportService;

	@Inject
	protected TopReportService m_topReportService;
	
	@Inject
	private ExceptionRuleConfigManager m_exceptionRuleConfigManager;

	@Inject
	private ServerConfigManager m_configManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		AlertReport alertReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);
		return m_alertReportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		AlertReport alertReport = new AlertReport(Constants.CAT);
		TopReportVisitor visitor = new TopReportVisitor().setReport(alertReport)
		      .setExceptionRuleConfigManager(m_exceptionRuleConfigManager).setConfigManager(m_configManager);
		Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);

		alertReport.setStartTime(start);
		alertReport.setEndTime(end);

		TopReport topReport = m_topReportService.queryReport(Constants.CAT, start, end);
		visitor.visitTopReport(topReport);

		HourlyReport report = new HourlyReport();
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);

		return m_alertReportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		AlertReport alertReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);

		return m_alertReportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		AlertReport alertReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeHelper.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(alertReport);

		return m_alertReportService.insertWeeklyReport(report, binaryContent);
	}

	private AlertReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		AlertReportMerger merger = new AlertReportMerger(new AlertReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				AlertReport reportModel = m_alertReportService.queryReport(domain, new Date(startTime), new Date(startTime
				      + TimeHelper.ONE_DAY));
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

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			AlertReport reportModel = m_alertReportService.queryReport(domain, date, new Date(date.getTime()
			      + TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		AlertReport alertReport = merger.getAlertReport();

		return alertReport;
	}
}
