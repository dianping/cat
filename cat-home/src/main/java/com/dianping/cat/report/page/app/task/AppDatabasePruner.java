package com.dianping.cat.report.page.app.task;

import java.util.Calendar;
import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.app.crash.CrashLog;
import com.dianping.cat.app.crash.CrashLogContent;
import com.dianping.cat.app.crash.CrashLogContentDao;
import com.dianping.cat.app.crash.CrashLogDao;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.TaskBuilder;

@Named(type = TaskBuilder.class, value = AppDatabasePruner.ID)
public class AppDatabasePruner implements TaskBuilder {

	public static final String ID = Constants.APP_DATABASE_PRUNER;

	@Inject
	private AppSpeedDataDao m_appSpeedDataDao;

	@Inject
	private AppSpeedConfigManager m_appSpeedConfigManager;

	@Inject
	private AppCommandDataDao m_appCommandDataDao;

	@Inject
	private CrashLogDao m_crashLogDao;

	@Inject
	private CrashLogContentDao m_crashLogContentDao;

	@Inject
	private AppCommandConfigManager m_appConfigManager;

	private static final int DURATION = -2;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Threads.forGroup("cat").start(new DeleteTask());

		return true;
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("AppDatabasePruner builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("AppDatabasePruner builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("AppDatabasePruner builder don't support weekly task");
	}

	public void pruneAppCommandTable(Date period, int id) throws DalException {
		AppCommandData appCommandData = m_appCommandDataDao.createLocal();

		appCommandData.setCommandId(id);
		appCommandData.setPeriod(period);
		m_appCommandDataDao.deleteBeforePeriod(appCommandData);
	}

	private boolean pruneAppCommndData(Date period) {
		boolean success = true;

		for (Command command : m_appConfigManager.queryCommands().values()) {
			Transaction t = Cat.newTransaction("DeleteTask", "App");
			try {
				pruneAppCommandTable(period, command.getId());
				t.setStatus(Transaction.SUCCESS);
			} catch (DalException e) {
				Cat.logError(e);
				t.setStatus(e);
				success = false;
			} finally {
				t.complete();
			}
		}
		return success;
	}

	private boolean pruneAppSpeedData(Date period) {
		boolean succes = true;

		for (Integer speedId : m_appSpeedConfigManager.querySpeedIds()) {
			Transaction t = Cat.newTransaction("DeleteTask", "Speed");
			try {
				pruneAppSpeedTable(period, speedId);
				t.setStatus(Transaction.SUCCESS);
			} catch (DalException e) {
				t.setStatus(e);
				Cat.logError(e);
				succes = false;
			} finally {
				t.complete();
			}
		}
		return succes;
	}

	public void pruneAppSpeedTable(Date period, int speedId) throws DalException {
		AppSpeedData appSpeedData = m_appSpeedDataDao.createLocal();

		appSpeedData.setSpeedId(speedId);
		appSpeedData.setPeriod(period);
		m_appSpeedDataDao.deleteBeforePeriod(appSpeedData);
	}

	public boolean pruneDatabase(int months) {
		Date period = queryPeriod(months);
		boolean command = pruneAppCommndData(period);
		boolean speed = pruneAppSpeedData(period);
		boolean crash = pruneCrashLog(period);

		return command && speed && crash;
	}

	public boolean pruneCrashLog(Date period) {
		boolean success = true;
		Transaction t = Cat.newTransaction("DeleteTask", "crashLog");

		try {
			CrashLog crashLog = m_crashLogDao.createLocal();
			crashLog.setUpdatetime(period);
			m_crashLogDao.deleteBeforePeriod(crashLog);

			CrashLogContent crashLogContent = m_crashLogContentDao.createLocal();
			crashLogContent.setUpdatetime(period);
			m_crashLogContentDao.deleteBeforePeriod(crashLogContent);

			t.setStatus(Transaction.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
			success = false;
		} finally {
			t.complete();
		}

		return success;
	}

	public Date queryPeriod(int months) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}

	public class DeleteTask implements Task {

		@Override
		public void run() {
			try {
				pruneDatabase(DURATION);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		@Override
		public String getName() {
			return "delete-app-job";
		}

		@Override
		public void shutdown() {
		}
	}

}
