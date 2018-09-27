package com.dianping.cat.report.page.overload.task;

import java.util.Date;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.report.task.TaskBuilder;

@Named(type = TaskBuilder.class, value = CapacityUpdateTask.ID)
public class CapacityUpdateTask implements TaskBuilder, LogEnabled {

	public static final String ID = Constants.REPORT_DATABASE_CAPACITY;

	@Inject(type = CapacityUpdater.class, value = HourlyCapacityUpdater.ID)
	private CapacityUpdater m_hourlyUpdater;

	@Inject(type = CapacityUpdater.class, value = DailyCapacityUpdater.ID)
	private CapacityUpdater m_dailyUpdater;

	@Inject(type = CapacityUpdater.class, value = WeeklyCapacityUpdater.ID)
	private CapacityUpdater m_weeklyUpdater;

	@Inject(type = CapacityUpdater.class, value = MonthlyCapacityUpdater.ID)
	private CapacityUpdater m_monthlyUpdater;

	protected Logger m_logger;

	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		try {
			m_dailyUpdater.updateDBCapacity();
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		try {
			m_hourlyUpdater.updateDBCapacity();
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		try {
			m_monthlyUpdater.updateDBCapacity();
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		try {
			m_weeklyUpdater.updateDBCapacity();
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

	public void setDailyUpdater(CapacityUpdater dailyUpdater) {
		m_dailyUpdater = dailyUpdater;
	}

	public void setHourlyUpdater(CapacityUpdater hourlyUpdater) {
		m_hourlyUpdater = hourlyUpdater;
	}

	public void setMonthlyUpdater(CapacityUpdater monthlyUpdater) {
		m_monthlyUpdater = monthlyUpdater;
	}

	public void setWeeklyUpdater(CapacityUpdater weeklyUpdater) {
		m_weeklyUpdater = weeklyUpdater;
	}

}
