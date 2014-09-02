package com.dianping.cat.report.task.overload;

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

	@Inject(type = CapacityUpdater.class, value = HourlyCapacityUpdater.ID)
	private CapacityUpdater m_hourlyUpdater;

	@Inject(type = CapacityUpdater.class, value = DailyCapacityUpdater.ID)
	private CapacityUpdater m_dailyUpdater;

	@Inject(type = CapacityUpdater.class, value = WeeklyCapacityUpdater.ID)
	private CapacityUpdater m_weeklyUpdater;

	@Inject(type = CapacityUpdater.class, value = MonthlyCapacityUpdater.ID)
	private CapacityUpdater m_monthlyUpdater;

	private Logger m_logger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
		String dayStr = String.valueOf(day);

		if (day < 10) {
			dayStr = "0" + dayStr;
		}

		try {
			m_dailyUpdater.updateDBCapacity(CAPACITY);

			Cat.logEvent("DailyCapacityUpdater", dayStr, Event.SUCCESS, null);
			m_logger.info("DailyCapacityUpdater success " + dayStr);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
		String hourStr = String.valueOf(hour);

		if (hour < 10) {
			hourStr = "0" + hourStr;
		}

		try {
			m_hourlyUpdater.updateDBCapacity(CAPACITY);

			Cat.logEvent("HourlyCapacityUpdater", hourStr, Event.SUCCESS, null);
			m_logger.info("HourlyCapacityUpdater success " + hourStr);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		int month = Calendar.getInstance().get(Calendar.MONTH);
		String monthStr = String.valueOf(month);

		if (month < 10) {
			monthStr = "0" + monthStr;
		}

		try {
			m_monthlyUpdater.updateDBCapacity(CAPACITY);

			Cat.logEvent("MonthlyCapacityUpdater", monthStr, Event.SUCCESS, null);
			m_logger.info("MonthlyCapacityUpdater success " + monthStr);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		int week = Calendar.getInstance().get(Calendar.WEEK_OF_MONTH);
		String weekStr = String.valueOf(week);

		try {
			m_weeklyUpdater.updateDBCapacity(CAPACITY);

			Cat.logEvent("WeeklyCapacityUpdater", weekStr, Event.SUCCESS, null);
			m_logger.info("WeeklyCapacityUpdater success " + weekStr);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
