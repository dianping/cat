package com.dianping.cat.report.page.app.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;

import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.alarm.service.AppAlarmRuleService;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppCommandDataEntity;
import com.dianping.cat.app.AppDailyReport;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.entity.Code;
import com.dianping.cat.home.app.transform.DefaultNativeBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.page.appstats.service.AppStatisticReportService;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

@Named(type = TaskBuilder.class, value = AppReportBuilder.ID)
public class AppReportBuilder implements TaskBuilder {

	@Inject
	private AppCommandDataDao m_dao;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	@Inject
	private AppStatisticReportService m_appReportService;

	@Inject
	private AppAlarmRuleService m_appAlarmRuleService;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	public static final String ID = Constants.APP;

	public static final int COMMAND_MIN_COUNT = 10;

	private AppReport buildDailyReport(String id, Date period) {
		AppReport report = m_appReportService.makeReport(id, period, TaskHelper.tomorrowZero(period));

		for (Command command : m_appConfigManager.queryCommands().values()) {
			if (id.equals(command.getNamespace())) {
				processCommand(period, command, report);
			}
		}
		return report;
	}

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		final String reportName = name;
		final String namespace = domain;
		final Date reportPeriod = period;

		Threads.forGroup("cat").start(new Threads.Task() {

			@Override
			public String getName() {
				return "app-report-task-" + namespace;
			}

			@Override
			public void run() {
				runDailyTask(reportName, namespace, reportPeriod);
			}

			@Override
			public void shutdown() {
			}
		});
		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("daily report builder don't support weekly task");
	}

	private void processCommand(Date period, Command command, AppReport report) {
		int commandId = command.getId();
		List<AppCommandData> datas = new ArrayList<AppCommandData>();
		com.dianping.cat.home.app.entity.Command cmd = report.findOrCreateCommand(command.getId());

		cmd.setName(command.getName());

		try {
			datas = m_dao.findDailyDataByCode(commandId, period, AppCommandDataEntity.READSET_CODE_DATA);

			for (AppCommandData data : datas) {
				processRecord(commandId, cmd, data);
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	private void processRecord(int commandId, com.dianping.cat.home.app.entity.Command cmd, AppCommandData data) {
		int codeId = data.getCode();
		boolean success = m_appConfigManager.isSuccessCode(commandId, codeId);
		long count = data.getAccessNumberSum();
		long responseTime = data.getResponseSumTimeSum();

		cmd.incCount(count).incSum(responseTime).incRequestSum(data.getRequestPackageSum())
		      .incResponseSum(data.getResponsePackageSum());

		Code code = cmd.findOrCreateCode(String.valueOf(codeId));

		code.incCount(count);
		code.incSum(responseTime);

		if (!success) {
			cmd.incErrors(count);
			code.incErrors(count);
		}
		long cmdCount = cmd.getCount();
		if (cmdCount > 0) {
			cmd.setAvg(cmd.getSum() / cmdCount);
			cmd.setSuccessRatio(100.0 - cmd.getErrors() * 100.0 / cmdCount);
			cmd.setRequestAvg(cmd.getRequestSum() * 1.0 / cmdCount);
			cmd.setResponseAvg(cmd.getResponseSum() * 1.0 / cmdCount);
		}
		long codeCount = code.getCount();
		if (codeCount > 0) {
			code.setAvg(code.getSum() / codeCount);
			code.setSuccessRatio(100.0 - code.getErrors() * 100.0 / codeCount);
		}
	}

	private void pruneAppCommand(AppReport appReport) {
		for (Entry<Integer, com.dianping.cat.home.app.entity.Command> command : appReport.getCommands().entrySet()) {
			if (command.getValue().getCount() < COMMAND_MIN_COUNT) {
				try {
					int id = command.getKey();
					String name = m_appConfigManager.getRawCommands().get(id).getName();
					boolean success = m_appConfigManager.deleteCommand(id);

					if (success) {
						Cat.logEvent("AppCommandPrune", id + ":" + name, Event.SUCCESS, command.toString());
						m_appAlarmRuleService.deleteByCommand(id);
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		}
	}

	public boolean runDailyTask(String name, String namespace, Date period) {
		try {
			AppReport appReport = buildDailyReport(namespace, period);

			if (m_mobileConfigManager.shouldAutoPrune()) {
				pruneAppCommand(appReport);
			}

			AppDailyReport report = new AppDailyReport();

			report.setCreationDate(new Date());
			report.setAppId(m_mobileConfigManager.queryNamespaceIdByTitle(namespace));
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(name);
			report.setPeriod(period);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(appReport);

			return m_appReportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}
}
