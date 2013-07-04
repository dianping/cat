package com.dianping.cat.report.task.state;

import java.util.Calendar;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.state.StateReportMerger;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportBuilder;

public class StateReportBuilder implements ReportBuilder {

	@Inject
	protected ReportService m_reportService;
	
	@Override
	public boolean buildDailyReport(String name, String domain, Date period) {
		DailyReport report = queryDailyReport(name, domain, period);

		return m_reportService.insertDailyReport(report);
	}

	@Override
	public boolean buildHourReport(String name, String domain, Date period) {
		throw new RuntimeException("State report don't support HourReport!");
	}

	private StateReport buildMergedDailyReport(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		StateReportMerger merger = new StateReportMerger(new StateReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {

				StateReport reportModel = m_reportService.queryStateReport(domain, new Date(startTime), new Date(startTime
				      + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		StateReport stateReport = merger.getStateReport();
		stateReport.setStartTime(start);
		stateReport.setEndTime(end);
		return stateReport;
	}

	@Override
	public boolean buildMonthReport(String name, String domain, Date period) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(period);
		cal.add(Calendar.MONTH, 1);

		Date start = period;
		Date end = cal.getTime();

		StateReport stateReport = buildMergedDailyReport(domain, start, end);
		MonthlyReport report = new MonthlyReport();

		report.setContent(stateReport.toString());
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

		StateReport stateReport = buildMergedDailyReport(domain, start, end);
		WeeklyReport report = new WeeklyReport();
		String content = stateReport.toString();

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
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		StateReportMerger merger = new StateReportMerger(new StateReport(domain));
		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);

			StateReport reportModel = m_reportService.queryStateReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));
			reportModel.accept(merger);

		}
		StateReport crossReport = merger.getStateReport();
		crossReport.setStartTime(period);
		crossReport.setEndTime(endDate);
		
		String content = crossReport.toString();
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
