package com.dianping.cat.report.task.state;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.consumer.state.model.transform.DefaultNativeBuilder;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.MonthlyReport;
import com.dianping.cat.core.dal.WeeklyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.TaskBuilder;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;

public class StateReportBuilder implements TaskBuilder {

	public static final String ID = StateAnalyzer.ID;

	@Inject
	protected ReportServiceManager m_reportService;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private HostinfoService m_hostinfoService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		StateReport stateReport = queryHourlyReportsByDuration(name, domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(stateReport);
		return m_reportService.insertDailyReport(report, binaryContent);
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		StateReport stateReport = m_reportService.queryStateReport(domain, period, new Date(period.getTime()
		      + TimeHelper.ONE_HOUR));

		new StateReportVisitor().visitStateReport(stateReport);

		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		StateReport stateReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(stateReport);
		return m_reportService.insertMonthlyReport(report, binaryContent);
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		Date start = period;
		Date end = new Date(start.getTime() + TimeHelper.ONE_DAY * 7);

		StateReport stateReport = queryDailyReportsByDuration(domain, start, end);
		WeeklyReport report = new WeeklyReport();

		report.setContent("");
		report.setCreationDate(new Date());
		report.setDomain(domain);
		report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		report.setName(name);
		report.setPeriod(period);
		report.setType(1);
		byte[] binaryContent = DefaultNativeBuilder.build(stateReport);
		return m_reportService.insertWeeklyReport(report, binaryContent);
	}

	private StateReport queryDailyReportsByDuration(String domain, Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();
		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport(domain));

		for (; startTime < endTime; startTime += TimeHelper.ONE_DAY) {
			try {
				StateReport reportModel = m_reportService.queryStateReport(domain, new Date(startTime), new Date(startTime
				      + TimeHelper.ONE_DAY));

				reportModel.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		StateReport stateReport = merger.getStateReport();

		new ClearDetailInfo().visitStateReport(stateReport);
		stateReport.setStartTime(start);
		stateReport.setEndTime(end);
		return stateReport;
	}

	private StateReport queryHourlyReportsByDuration(String name, String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			StateReport reportModel = m_reportService.queryStateReport(domain, date, new Date(date.getTime()
			      + TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		StateReport stateReport = merger.getStateReport();

		new ClearDetailInfo().visitStateReport(stateReport);
		stateReport.setStartTime(period);
		stateReport.setEndTime(endDate);
		return stateReport;
	}

	private void updateDomainInfo(String domain, String ip) {
		if (m_serverConfigManager.validateDomain(domain)) {
			if (!m_projectService.containsDomainInCat(domain)) {
				m_projectService.insertDomain(domain);

			}
			Hostinfo info = m_hostinfoService.findByIp(ip);

			if (info == null) {
				m_hostinfoService.insert(domain, ip);
			} else {
				String oldDomain = info.getDomain();

				if (!domain.equals(oldDomain) && !Constants.CAT.equals(oldDomain)) {
					m_hostinfoService.update(info.getId(), domain, ip);
				}
			}
		}
	}

	public static class ClearDetailInfo extends BaseVisitor {

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			processDomain.getDetails().clear();
		}
	}

	public class StateReportVisitor extends BaseVisitor {

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			String domain = processDomain.getName();
			Set<String> ips = processDomain.getIps();

			for (String ip : ips) {
				if (m_serverConfigManager.validateDomain(domain) && m_serverConfigManager.validateIp(ip)) {
					updateDomainInfo(domain, ip);
				}
			}
		}
	}

}
