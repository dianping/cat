package com.dianping.cat.report.task.utilization;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.DomainManager;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
import com.dianping.cat.report.page.transaction.TransactionMergeManager;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class UtilizationReportBuilder implements ReportTaskBuilder {
	
	public static final String ID = Constants.REPORT_UTILIZATION;

	@Inject
	protected ReportService m_reportService;

	@Inject
	private TransactionMergeManager m_mergeManager;

	@Inject
	private ServerConfigManager m_configManger;

	@Inject
	private DomainManager m_domainManager;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		UtilizationReport utilizationReport = queryHourlyReportsByDuration(name, domain, period,
		      TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();
		
		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);

		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date start) {
		UtilizationReport utilizationReport = new UtilizationReport(Constants.CAT);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, TransactionAnalyzer.ID);
		TransactionReportVisitor transactionVisitor = new TransactionReportVisitor()
		      .setUtilizationReport(utilizationReport);
		HeartbeatReportVisitor heartbeatVisitor = new HeartbeatReportVisitor().setUtilizationReport(utilizationReport);

		for (String domainName : domains) {
			if (m_configManger.validateDomain(domainName)) {
				TransactionReport transactionReport = m_reportService.queryTransactionReport(domainName, start, end);
				int size = transactionReport.getMachines().size();
				
				utilizationReport.findOrCreateDomain(domainName).setMachineNumber(size);
				transactionReport = m_mergeManager.mergerAllIp(transactionReport, Constants.ALL);
				transactionVisitor.visitTransactionReport(transactionReport);
			}
		}

		for (String domainName : domains) {
			if (m_configManger.validateDomain(domainName)) {
				HeartbeatReport heartbeatReport = m_reportService.queryHeartbeatReport(domainName, start, end);

				heartbeatVisitor.visitHeartbeatReport(heartbeatReport);
			}
		}

		for (String domainName : domains) {
			if (m_configManger.validateDomain(domainName)) {
				CrossReport crossReport = m_reportService.queryCrossReport(domainName, start, end);
				ProjectInfo projectInfo = new ProjectInfo(TimeUtil.ONE_HOUR);

				projectInfo.setDomainManager(m_domainManager);
				projectInfo.setClientIp(Constants.ALL);
				projectInfo.visitCrossReport(crossReport);
				Collection<TypeDetailInfo> callInfos = projectInfo.getCallProjectsInfo();

				for (TypeDetailInfo typeInfo : callInfos) {
					String project = typeInfo.getProjectName();

					if (!validataService(project)) {
						long failure = typeInfo.getFailureCount();
						Domain d = utilizationReport.findOrCreateDomain(project);
						ApplicationState service = d.findApplicationState("PigeonService");

						if (service != null) {
							service.setFailureCount(service.getFailureCount() + failure);
							
							long count = service.getCount();
							if (count > 0) {
								service.setFailurePercent(service.getFailureCount() * 1.0 / count);
							}
						}
					}
				}
			}
		}

		utilizationReport.setStartTime(start);
		utilizationReport.setEndTime(end);

		HourlyReport report = new HourlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);

		return m_reportService.insertHourlyReport(report, binaryContent);
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		UtilizationReport utilizationReport = queryDailyReportsByDuration(domain, period,
		      TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);

		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		UtilizationReport utilizationReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(utilizationReport);

		return m_reportService.insertWeeklyReport(report, binaryContent);
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

	private boolean validataService(String projectName) {
		return projectName.equalsIgnoreCase(ProjectInfo.ALL_SERVER) || projectName.equalsIgnoreCase("UnknownProject");
	}

}
