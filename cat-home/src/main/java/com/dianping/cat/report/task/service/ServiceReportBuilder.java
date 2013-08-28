package com.dianping.cat.report.task.service;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.DomainManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.report.page.cross.display.ProjectInfo;
import com.dianping.cat.report.page.cross.display.TypeDetailInfo;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class ServiceReportBuilder implements ReportTaskBuilder {

	@Inject
	protected ReportService m_reportService;

	@Inject
	private DomainManager m_domainManager;

	Map<String, Domain> stat = new HashMap<String, Domain>();

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		ServiceReport serviceReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));

		DailyReport report = new DailyReport();
		report.setContent(serviceReport.toString());
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
		ServiceReport serviceReport = new ServiceReport(Constants.CAT);
		Date end = new Date(start.getTime() + TimeUtil.ONE_HOUR);
		Set<String> domains = m_reportService.queryAllDomainNames(start, end, CrossAnalyzer.ID);

		for (String domainName : domains) {
			CrossReport crossReport = m_reportService.queryCrossReport(domainName, start, end);
			ProjectInfo projectInfo = new ProjectInfo(TimeUtil.ONE_HOUR);

			projectInfo.setDomainManager(m_domainManager);
			projectInfo.setClientIp(Constants.ALL);
			projectInfo.visitCrossReport(crossReport);
			Collection<TypeDetailInfo> callInfos = projectInfo.getCallProjectsInfo();

			for (TypeDetailInfo typeInfo : callInfos) {
				if (!validataService(typeInfo)) {
					merge(serviceReport.findOrCreateDomain(typeInfo.getProjectName()), typeInfo);
				}
			}
		}
		HourlyReport report = new HourlyReport();

		report.setContent(serviceReport.toString());
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(start);
		report.setType(1);
		return m_reportService.insertHourlyReport(report);
	}

	public void merge(Domain domain, TypeDetailInfo info) {
		domain.setTotalCount(domain.getTotalCount() + info.getTotalCount());
		domain.setFailureCount(domain.getFailureCount() + info.getFailureCount());
		domain.setSum(domain.getSum() + info.getSum());
		if (domain.getTotalCount() > 0) {
			domain.setAvg(domain.getSum() / domain.getTotalCount());
			domain.setFailurePercent((double) domain.getFailureCount() / domain.getTotalCount());
		}
	}

	private boolean validataService(TypeDetailInfo typeInfo) {
		return typeInfo.getProjectName().equalsIgnoreCase(ProjectInfo.ALL_SERVER)
		      || typeInfo.getProjectName().equalsIgnoreCase("UnknownProject");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		ServiceReport serviceReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent(serviceReport.toString());
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
		ServiceReport serviceReport = queryDailyReportsByDuration(domain, period, new Date(period.getTime()
		      + TimeUtil.ONE_WEEK));
		WeeklyReport report = new WeeklyReport();
		String content = serviceReport.toString();

		report.setContent(content);
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		return m_reportService.insertWeeklyReport(report);
	}

	private ServiceReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		ServiceReportMerger merger = new ServiceReportMerger(new ServiceReport(domain));

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			try {
				ServiceReport reportModel = m_reportService.queryServiceReport(domain, new Date(startTime), new Date(
				      startTime + TimeUtil.ONE_DAY));
				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		ServiceReport serviceReport = merger.getServiceReport();

		serviceReport.setStartTime(start);
		serviceReport.setEndTime(end);
		return serviceReport;
	}

	private ServiceReport queryHourlyReportsByDuration(String name, String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		ServiceReportMerger merger = new ServiceReportMerger(new ServiceReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeUtil.ONE_HOUR) {
			Date date = new Date(startTime);
			ServiceReport reportModel = m_reportService.queryServiceReport(domain, date, new Date(date.getTime()
			      + TimeUtil.ONE_HOUR));

			reportModel.accept(merger);
		}

		ServiceReport serviceReport = merger.getServiceReport();

		serviceReport.setStartTime(start);
		serviceReport.setEndTime(end);
		return serviceReport;
	}

}
