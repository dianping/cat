package com.dianping.cat.report.task.database;

import java.util.Calendar;
import java.util.Date;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.message.Event;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class CapacityUpdateTask implements ReportTaskBuilder, LogEnabled {

	public static final double CAPACITY = 5.0;

	public static final String ID = Constants.DATABASE_CAPACITY;

	@Inject
	private TableCapacityService m_tableCapacityService;

	private Logger m_logger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		String updaterName = DailyCapacityUpdater.ID;
		CapacityUpdater updater = m_tableCapacityService.getUpdater(updaterName);

		if (updater != null) {
			int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
			String dayStr = String.valueOf(day);

			if (day < 10) {
				dayStr = "0" + dayStr;
			}

			try {
				int maxId = updater.updateDBCapacity(CAPACITY);
				updater.updateOverloadReport(maxId, m_tableCapacityService.getOverloadReports());

				Cat.logEvent("DailyCapacityUpdater", dayStr, Event.SUCCESS, null);
				m_logger.info("DailyCapacityUpdater success " + dayStr);
				return true;
			} catch (DalException e) {
				Cat.logError(e);
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		String updaterName = HourlyCapacityUpdater.ID;
		CapacityUpdater updater = m_tableCapacityService.getUpdater(updaterName);

		if (updater != null) {
			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
			String hourStr = String.valueOf(hour);

			if (hour < 10) {
				hourStr = "0" + hourStr;
			}

			try {
				int maxId = updater.updateDBCapacity(CAPACITY);
				updater.updateOverloadReport(maxId, m_tableCapacityService.getOverloadReports());

				Cat.logEvent("HourlyCapacityUpdater", hourStr, Event.SUCCESS, null);
				m_logger.info("HourlyCapacityUpdater success " + hourStr);
				return true;
			} catch (DalException e) {
				Cat.logError(e);
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		String updaterName = MonthlyCapacityUpdater.ID;
		CapacityUpdater updater = m_tableCapacityService.getUpdater(updaterName);

		if (updater != null) {
			int month = Calendar.getInstance().get(Calendar.MONTH);
			String monthStr = String.valueOf(month);

			if (month < 10) {
				monthStr = "0" + monthStr;
			}

			try {
				int maxId = updater.updateDBCapacity(CAPACITY);
				updater.updateOverloadReport(maxId, m_tableCapacityService.getOverloadReports());

				Cat.logEvent("MonthlyCapacityUpdater", monthStr, Event.SUCCESS, null);
				m_logger.info("MonthlyCapacityUpdater success " + monthStr);
				return true;
			} catch (DalException e) {
				Cat.logError(e);
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		String updaterName = WeeklyCapacityUpdater.ID;
		CapacityUpdater updater = m_tableCapacityService.getUpdater(updaterName);

		if (updater != null) {
			int week = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH);
			String weekStr = String.valueOf(week);

			try {
				int maxId = updater.updateDBCapacity(CAPACITY);
				updater.updateOverloadReport(maxId, m_tableCapacityService.getOverloadReports());

				Cat.logEvent("WeeklyCapacityUpdater", weekStr, Event.SUCCESS, null);
				m_logger.info("WeeklyCapacityUpdater success " + weekStr);
				return true;
			} catch (DalException e) {
				Cat.logError(e);
				return false;
			}
		} else {
			return false;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
