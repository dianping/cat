package com.dianping.cat.report.task.utilization;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.report.page.transaction.TransactionMergeManager;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class UtilizationReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Inject
	private TransactionMergeManager m_mergeManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		UtilizationReport utilizationReport = queryHourlyReportsByDuration(name, domain, period,
		      TaskHelper.tomorrowZero(period));

		DailyReport report = new DailyReport();
		report.setContent(utilizationReport.toString());
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
		UtilizationReport utilizationReport = new UtilizationReport();
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, "matrix");
		TransactionReportVisitor visitor = new TransactionReportVisitor().setReport(utilizationReport);

		for (String domainName : domains) {
			TransactionReport transactionReport = m_reportService.queryTransactionReport(domainName, start, end);
			int size = transactionReport.getMachines().size();
			
			transactionReport = m_mergeManager.mergerAllIp(transactionReport, CatString.ALL);
			visitor.visitTransactionReport(transactionReport);
			utilizationReport.findOrCreateDomain(domainName).setMachineNumber(size);
		}
		HourlyReport report = new HourlyReport();

		System.out.println(utilizationReport);

		report.setContent(utilizationReport.toString());
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
		UtilizationReport utilizationReport = queryDailyReportsByDuration(domain, period,
		      TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent(utilizationReport.toString());
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
		UtilizationReport utilizationReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();
		String content = utilizationReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertWeeklyReport(report);
	}

	private UtilizationReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				UtilizationReport reportModel = m_reportService.queryUtilizationReport(domain, new Date(startTime),
				      new Date(startTime + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);
		return utilizationReport;
	}

	private UtilizationReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			UtilizationReport reportModel = m_reportService.queryUtilizationReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));

			reportModel.accept(merger);
		}
		UtilizationReport utilizationReport = merger.getUtilizationReport();

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);
		return utilizationReport;
	}

}
