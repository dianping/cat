package com.dianping.cat.report.page.matrix.task;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixReportFilter;
import com.dianping.cat.consumer.matrix.MatrixReportMerger;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class MatrixReportBuilder implements TaskBuilder {

	public static final String ID = MatrixAnalyzer.ID;

	@Inject
	protected MatrixReportService m_reportService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		MatrixReport matrixReport = queryHourlyReportByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(matrixReport);
		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("Matrix report don't support hourly report!");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		MatrixReport matrixReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(matrixReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		MatrixReport matrixReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeHelper.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(matrixReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private MatrixReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				MatrixReport reportModel = m_reportService.queryReport(domain, new Date(startTime), new Date(
				      startTime + TimeHelper.ONE_DAY));

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		MatrixReport matrixReport = merger.getMatrixReport();
		new MatrixReportFilter().visitMatrixReport(matrixReport);

		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		return matrixReport;
	}

	private MatrixReport queryHourlyReportByDuration(String name, String domain, Date start, Date end) {
		Set<String> domainSet = m_reportService.queryAllDomainNames(start, end, MatrixAnalyzer.ID);
		long startTime = start.getTime();
		long endTime = end.getTime();
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			MatrixReport reportModel = m_reportService.queryReport(domain, date, new Date(date.getTime()
			      + TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		MatrixReport matrixReport = merger.getMatrixReport();
		new MatrixReportFilter().visitMatrixReport(matrixReport);

		matrixReport.getDomainNames().addAll(domainSet);
		matrixReport.setStartTime(start);
		matrixReport.setEndTime(end);
		return matrixReport;
	}

}
