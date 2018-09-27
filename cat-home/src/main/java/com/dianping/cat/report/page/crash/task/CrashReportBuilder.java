package com.dianping.cat.report.page.crash.task;

import java.util.Date;
import java.util.List;

import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppDailyReport;
import com.dianping.cat.app.crash.CrashLog;
import com.dianping.cat.app.crash.CrashLogDao;
import com.dianping.cat.app.crash.CrashLogEntity;
import com.dianping.cat.config.app.MobileConfigManager;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.home.crash.entity.App;
import com.dianping.cat.home.crash.entity.CrashReport;
import com.dianping.cat.home.crash.entity.Module;
import com.dianping.cat.home.crash.entity.Version;
import com.dianping.cat.home.crash.transform.DefaultNativeBuilder;
import com.dianping.cat.report.page.crash.service.CrashStatisticReportService;
import com.dianping.cat.report.task.TaskBuilder;
import com.dianping.cat.report.task.TaskHelper;

@Named(type = TaskBuilder.class, value = CrashReportBuilder.ID)
public class CrashReportBuilder implements TaskBuilder {

	public static final String ID = Constants.CRASH;

	@Inject
	private MobileConfigManager m_mobileConfigManager;

	@Inject
	private CrashStatisticReportService m_crashReportService;

	@Inject
	private CrashLogDao m_crashLogDao;

	private final int LIMIT = 5000;

	@Override
	public boolean buildDailyTask(final String name, final String domain, final Date period) {

		Threads.forGroup("cat").start(new Threads.Task() {

			@Override
			public void run() {
				runDailyTask(name, domain, period);
			}

			@Override
			public void shutdown() {
			}

			@Override
			public String getName() {
				return "crash-report-task-" + domain;
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

	public boolean runDailyTask(String reportName, String appName, Date reportPeriod) {
		try {
			int appId = m_mobileConfigManager.queryNamespaceIdByTitle(appName);
			CrashReport crashReport = buildDailyReport(appId, reportPeriod);

			AppDailyReport report = new AppDailyReport();

			report.setCreationDate(new Date());
			report.setAppId(appId);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(reportName);
			report.setPeriod(reportPeriod);
			report.setType(1);
			byte[] binaryContent = DefaultNativeBuilder.build(crashReport);

			return m_crashReportService.insertDailyReport(report, binaryContent);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private CrashReport buildDailyReport(int appId, Date reportPeriod) {
		Date end = TaskHelper.tomorrowZero(reportPeriod);
		CrashReport crashReport = m_crashReportService.makeReport(String.valueOf(appId), reportPeriod, end);

		try {
			int offset = 0;

			while (true) {
				List<CrashLog> result = m_crashLogDao.findDataByConditions(reportPeriod, end, String.valueOf(appId), -1,
				      null, offset, LIMIT, CrashLogEntity.READSET_FULL);

				for (CrashLog log : result) {
					App app = crashReport.findOrCreateApp(log.getPlatform());
					Version version = app.findOrCreateVersion(log.getAppVersion());
					Module module = version.findOrCreateModule(log.getModule());

					version.incCrashCount();
					module.incCrashCount();
				}

				int count = result.size();
				offset += count;

				if (count < LIMIT) {
					break;
				}
			}

			for (App app : crashReport.getApps().values()) {
				for (Version version : app.getVersions().values()) {
					// TODO: DAU interface
					int dau = 0;
					double percent = dau == 0 ? 0 : ((version.getCrashCount() + 0.0) / dau) * 100;
					version.setDau(dau);
					version.setPercent(percent);
				}
			}

		} catch (Exception e) {
			Cat.logError(e);
		}
		return crashReport;
	}

}
