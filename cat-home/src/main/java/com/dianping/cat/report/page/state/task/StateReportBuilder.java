package com.dianping.cat.report.page.state.task;

import java.util.Date;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
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
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;

public class StateReportBuilder implements TaskBuilder {

	public static final String ID = StateAnalyzer.ID;

	@Inject
	protected StateReportService m_reportService;

	@Inject
	protected ServerConfigManager m_serverConfigManager;

	@Inject
	protected ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private ProjectService m_projectService;

	@Inject
	private HostinfoService m_hostinfoService;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		StateReport stateReport = queryHourlyReportsByDuration(domain, period, TaskHelper.tomorrowZero(period));
		DailyReport report = new DailyReport();

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
		StateReport stateReport = m_reportService.queryReport(domain, period, new Date(period.getTime()
		      + TimeHelper.ONE_HOUR));

		new StateReportVisitor().visitStateReport(stateReport);

		return true;
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		StateReport stateReport = queryDailyReportsByDuration(domain, period, TaskHelper.nextMonthStart(period));
		MonthlyReport report = new MonthlyReport();

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
				StateReport reportModel = m_reportService.queryReport(domain, new Date(startTime), new Date(startTime
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

	private StateReport queryHourlyReportsByDuration(String domain, Date period, Date endDate) {
		long startTime = period.getTime();
		long endTime = endDate.getTime();
		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport(domain));

		for (; startTime < endTime; startTime = startTime + TimeHelper.ONE_HOUR) {
			Date date = new Date(startTime);
			StateReport reportModel = m_reportService.queryReport(domain, date, new Date(date.getTime()
			      + TimeHelper.ONE_HOUR));

			reportModel.accept(merger);
		}
		StateReport stateReport = merger.getStateReport();

		new ClearDetailInfo().visitStateReport(stateReport);
		stateReport.setStartTime(period);
		stateReport.setEndTime(endDate);
		return stateReport;
	}

	private void updateProjectAndHost(String domain, String ip) {
		if (m_serverFilterConfigManager.validateDomain(domain)) {
			if (!m_projectService.contains(domain)) {
				m_projectService.insert(domain);

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
				if (m_serverFilterConfigManager.validateDomain(domain) && m_serverConfigManager.validateIp(ip)) {
					updateProjectAndHost(domain, ip);
				}
			}
		}
	}

}
