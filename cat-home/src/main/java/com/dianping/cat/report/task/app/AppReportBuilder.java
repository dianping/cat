package com.dianping.cat.report.task.app;

import java.util.Date;
import java.util.List;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppCommandDataEntity;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.core.dal.DailyReport;
import com.dianping.cat.home.app.entity.AppReport;
import com.dianping.cat.home.app.entity.Code;
import com.dianping.cat.home.app.transform.DefaultNativeBuilder;
import com.dianping.cat.report.service.impl.AppReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

public class AppReportBuilder implements TaskBuilder {

	@Inject
	private AppCommandDataDao m_dao;

	@Inject
	private AppConfigManager m_appConfigManager;

	@Inject
	private AppReportService m_appReportService;

	public static final String ID = Constants.APP;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			AppReport appReport = buildDailyReport(domain, period);
			System.out.println(appReport);
			DailyReport report = new DailyReport();

			report.setCreationDate(new Date());
			report.setDomain(domain);
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

	private AppReport buildDailyReport(String id, Date period) {
		AppReport report = m_appReportService.makeReport(id, period, TaskHelper.tomorrowZero(period));

		for (Command command : m_appConfigManager.queryCommands()) {
			processCommand(period, command, report);
		}
		return report;
	}

	private void processCommand(Date period, Command command, AppReport report) {
		int commandId = command.getId();
		com.dianping.cat.home.app.entity.Command cmd = report.findOrCreateCommand(String.valueOf(command.getName()));
		cmd.setDomain(command.getDomain());
		cmd.setTitle(command.getTitle());

		try {
			List<AppCommandData> datas = m_dao.findDailyDataByCode(commandId, period,
			      AppCommandDataEntity.READSET_CODE_DATA);

			for (AppCommandData data : datas) {
				int codeId = data.getCode();
				boolean success = m_appConfigManager.isSuccessCode(commandId, codeId);
				long count = data.getAccessNumberSum();
				long responseTime = data.getResponseSumTimeSum();

				cmd.incCount(count);
				cmd.incSum(responseTime);

				Code code = cmd.findOrCreateCode(String.valueOf(codeId));

				code.setTitle(code.getTitle());
				code.incCount(count);
				code.incSum(responseTime);

				if (!success) {
					cmd.incErrors(count);
					code.incErrors(count);
				}
				if (cmd.getCount() > 0) {
					cmd.setAvg(cmd.getSum() / cmd.getCount());
					cmd.setErrorPercent(cmd.getErrors() * 1.0 / cmd.getCount());
				}
				if (code.getCount() > 0) {
					code.setAvg(code.getSum() / code.getCount());
					code.setErrorPercent(code.getErrors() * 1.0 / code.getCount());
				}
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
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

}
