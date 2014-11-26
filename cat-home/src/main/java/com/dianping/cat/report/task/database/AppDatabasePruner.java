package com.dianping.cat.report.task.database;

import java.util.Calendar;
import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.config.app.AppSpeedConfigManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class AppDatabasePruner implements ReportTaskBuilder {

	public static final String ID = Constants.APP_DATABASE_PRUNER;

	@Inject
	private AppSpeedDataDao m_appSpeedDataDao;

	@Inject
	private AppSpeedConfigManager m_appSpeedConfigManager;

	@Inject
	private AppCommandDataDao m_appCommandDataDao;

	@Inject
	private AppConfigManager m_appConfigManager;

	private static final int DURATION = -3;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		return pruneDatabase(DURATION);
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

	public void pruneAppCommandTable(Date period, int id) throws DalException {
		AppCommandData appCommandData = new AppCommandDataDao().createLocal();

		appCommandData.setCommandId(id);
		appCommandData.setPeriod(period);
		m_appCommandDataDao.deleteBeforePeriod(appCommandData);
	}

	private boolean pruneAppCommndData(Date period) {
		boolean success = true;

		for (Command command : m_appConfigManager.queryCommands()) {
			try {
				pruneAppCommandTable(period, command.getId());
			} catch (DalException e) {
				Cat.logError(e);
				success = false;
			}
		}
		return success;
	}

	private boolean pruneAppSpeedData(Date period) {
		boolean succes = true;

		for (Integer speedId : m_appSpeedConfigManager.querySpeedIds()) {
			try {
				pruneAppSpeedTable(period, speedId);
			} catch (DalException e) {
				Cat.logError(e);
				succes = false;
			}
		}
		return succes;
	}

	public void pruneAppSpeedTable(Date period, int speedId) throws DalException {
		AppSpeedData appSpeedData = new AppSpeedDataDao().createLocal();

		appSpeedData.setSpeedId(speedId);
		appSpeedData.setPeriod(period);
		m_appSpeedDataDao.deleteBeforePeriod(appSpeedData);
	}

	public boolean pruneDatabase(int months) {
		Date period = queryPeriod(months);
		boolean command = pruneAppCommndData(period);
		boolean speed = pruneAppSpeedData(period);

		return command && speed;
	}

	public Date queryPeriod(int months) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.add(Calendar.MONTH, months);
		return cal.getTime();
	}

}
