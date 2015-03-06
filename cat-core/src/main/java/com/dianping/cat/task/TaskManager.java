package com.dianping.cat.task;

import java.util.Calendar;
import java.util.Date;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.core.dal.TaskDao;

public class TaskManager {

	@Inject
	private TaskDao m_taskDao;

	private static final long ONE_HOUR = 60 * 60 * 1000L;

	private static final long ONE_DAY = 24 * ONE_HOUR;

	private static final int STATUS_TODO = 1;

	public static final int REPORT_HOUR = 0;

	public static final int REPORT_DAILY = 1;

	public static final int REPORT_WEEK = 2;

	public static final int REPORT_MONTH = 3;

	private void createDailyTask(Date period, String domain, String name) throws DalException {
		createTask(period, domain, name, REPORT_DAILY);
	}

	private void createHourlyTask(Date period, String domain, String name) throws DalException {
		createTask(period, domain, name, REPORT_HOUR);
	}

	private void createMonthlyTask(Date period, String domain, String name) throws DalException {
		createTask(period, domain, name, REPORT_MONTH);
	}

	protected void createTask(Date period, String domain, String name, int reportType) throws DalException {
		Task task = m_taskDao.createLocal();

		task.setCreationDate(new Date());
		task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		task.setReportDomain(domain);
		task.setReportName(name);
		task.setReportPeriod(period);
		task.setStatus(STATUS_TODO);
		task.setTaskType(reportType);
		m_taskDao.insert(task);
	}

	public boolean createTask(Date period, String domain, String name, TaskCreationPolicy prolicy) {
		try {
			if (prolicy.shouldCreateHourlyTask()) {
				createHourlyTask(period, domain, name);
			}

			Calendar cal = Calendar.getInstance();
			cal.setTime(period);

			int hour = cal.get(Calendar.HOUR_OF_DAY);
			cal.add(Calendar.HOUR_OF_DAY, -hour);
			Date currentDay = cal.getTime();

			if (prolicy.shouldCreateDailyTask()) {
				createDailyTask(new Date(currentDay.getTime() - ONE_DAY), domain, name);
			}

			if (prolicy.shouldCreateWeeklyTask()) {
				int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
				if (dayOfWeek == 7) {
					createWeeklyTask(new Date(currentDay.getTime() - 7 * ONE_DAY), domain, name);
				}
			}
			if (prolicy.shouldCreateMonthTask()) {
				int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

				if (dayOfMonth == 1) {
					cal.add(Calendar.MONTH, -1);
					createMonthlyTask(cal.getTime(), domain, name);
				}
			}
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	private void createWeeklyTask(Date period, String domain, String name) throws DalException {
		createTask(period, domain, name, REPORT_WEEK);

	}

	public static interface TaskCreationPolicy {

		boolean shouldCreateHourlyTask();

		boolean shouldCreateDailyTask();

		boolean shouldCreateWeeklyTask();

		boolean shouldCreateMonthTask();
	}

	public enum TaskProlicy implements TaskCreationPolicy {

		ALL {
			@Override
			public boolean shouldCreateDailyTask() {
				return true;
			}

			@Override
			public boolean shouldCreateHourlyTask() {
				return true;
			}

			@Override
			public boolean shouldCreateMonthTask() {
				return true;
			}

			@Override
			public boolean shouldCreateWeeklyTask() {
				return true;
			}
		},

		HOULY {

			@Override
			public boolean shouldCreateDailyTask() {
				return false;
			}

			@Override
			public boolean shouldCreateHourlyTask() {
				return true;
			}

			@Override
			public boolean shouldCreateMonthTask() {
				return false;
			}

			@Override
			public boolean shouldCreateWeeklyTask() {
				return false;
			}
		},

		ALL_EXCLUED_HOURLY {

			@Override
			public boolean shouldCreateDailyTask() {
				return true;
			}

			@Override
			public boolean shouldCreateHourlyTask() {
				return false;
			}

			@Override
			public boolean shouldCreateMonthTask() {
				return true;
			}

			@Override
			public boolean shouldCreateWeeklyTask() {
				return true;
			}
		},

		DAILY {

			@Override
			public boolean shouldCreateDailyTask() {
				return true;
			}

			@Override
			public boolean shouldCreateHourlyTask() {
				return false;
			}

			@Override
			public boolean shouldCreateMonthTask() {
				return false;
			}

			@Override
			public boolean shouldCreateWeeklyTask() {
				return false;
			}
		},

		HOURLY_AND_DAILY {

			@Override
			public boolean shouldCreateDailyTask() {
				return true;
			}

			@Override
			public boolean shouldCreateHourlyTask() {
				return true;
			}

			@Override
			public boolean shouldCreateMonthTask() {
				return false;
			}

			@Override
			public boolean shouldCreateWeeklyTask() {
				return false;
			}
		};
	}

}
