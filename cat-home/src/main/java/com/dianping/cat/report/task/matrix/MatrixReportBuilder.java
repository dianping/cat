package com.dianping.cat.report.task.matrix;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.matrix.MatrixReportMerger;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class MatrixReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		MatrixReport matrixReport = queryHourlyReportByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setContent(matrixReport.toString());
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
		throw new RuntimeException("Matrix report don't support hourly report!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		MatrixReport matrixReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent(matrixReport.toString());
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
		MatrixReport matrixReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();
		String content = matrixReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertWeeklyReport(report);
	}

	private MatrixReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				MatrixReport reportModel = m_reportService.queryMatrixReport(domain, new Date(startTime), new Date(
				      startTime + TimeUtil.ONE_DAY));

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		MatrixReport matrixReport = merger.getMatrixReport();
		
		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		return matrixReport;
	}

	private MatrixReport queryHourlyReportByDuration(String name, String domain, Date start, Date end) {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, end, "matrix");
		long startTime = start.getTime();
		long endTime = end.getTime();
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			MatrixReport reportModel = m_reportService.queryMatrixReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));

			reportModel.accept(merger);
		}
		MatrixReport matrixReport = merger.getMatrixReport();

		matrixReport.getDomainNames().addAll(domainSet);
		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		return matrixReport;
	}

}
