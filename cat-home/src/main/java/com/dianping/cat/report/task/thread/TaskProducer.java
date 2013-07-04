package com.dianping.cat.report.task.thread;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.core.dal.TaskDao;
import com.dianping.cat.core.dal.TaskEntity;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportFacade;

public class TaskProducer implements org.unidal.helper.Threads.Task, Initializable {

	@Inject
	private ReportService m_reportService;

	@Inject
	private TaskDao m_taskDao;

	private Set<String> m_dailyReportNameSet = new HashSet<String>();

	private Set<String> m_graphReportNameSet = new HashSet<String>();

	private static final String STATE = "state";

	private long m_currentDay;

	private void createDailyReportTasks(Date date) {
		generateReportTasks(date, new Date(date.getTime() + TimeUtil.ONE_DAY), ReportFacade.TYPE_DAILY);
	}

	private void createMonthReportTasks(Date date) {
		Calendar cal = Calendar.getInstance();

		cal.setTime(date);

		int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
		if (dayOfMonth == 1) {
			Calendar monthEnd = Calendar.getInstance();

			monthEnd.setTime(date);
			monthEnd.add(Calendar.MONTH, 1);
			generateReportTasks(date, monthEnd.getTime(), ReportFacade.TYPE_WEEK);
		}
	}

	private void createWeeklyReportTasks(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);

		int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		if (dayOfWeek == 7) {
			generateReportTasks(date, new Date(date.getTime() + TimeUtil.ONE_DAY * 7), ReportFacade.TYPE_WEEK);
		}
	}

	private void creatReportTask(Date date) {
		createDailyReportTasks(date);
		createWeeklyReportTasks(date);
		createMonthReportTasks(date);
	}

	private void generateReportTasks(Date start, Date end, int reportType) {
		Set<String> domainSet = queryDomainSet(start, end);

		for (String domain : domainSet) {
			for (String name : m_dailyReportNameSet) {
				insertTask(domain, name, reportType, start);
			}
		}
		insertTask(CatString.CAT, STATE, reportType, start);
	}

	@Override
	public String getName() {
		return "Task-Producer";
	}

	@Override
	public void initialize() throws InitializationException {
		m_dailyReportNameSet.add("transaction");
		m_dailyReportNameSet.add("event");
		m_dailyReportNameSet.add("problem");
		m_dailyReportNameSet.add("matrix");
		m_dailyReportNameSet.add("cross");
		m_dailyReportNameSet.add("sql");

		m_graphReportNameSet.add("transaction");
		m_graphReportNameSet.add("event");
		m_graphReportNameSet.add("problem");
		m_graphReportNameSet.add("heartbeat");
	}

	private void insertTask(String domain, String reportName, int taskType, Date taskPeriod) {
		try {
			m_taskDao.findByDomainNameTypePeriod(reportName, domain, taskType, taskPeriod, TaskEntity.READSET_FULL);
		} catch (DalNotFoundException e) {
			Task task = m_taskDao.createLocal();

			task.setCreationDate(new Date());
			task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			task.setReportDomain(domain);
			task.setReportName(reportName);
			task.setReportPeriod(taskPeriod);
			task.setStatus(1);
			task.setTaskType(taskType);

			try {
				m_taskDao.insert(task);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private Set<String> queryDomainSet(Date start, Date end) {
		return m_reportService.queryAllDomainNames(start, end, "transaction");
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			Date currentDay = TimeUtil.getCurrentDay();

			if (currentDay.getTime() > m_currentDay) {
				Calendar cal = Calendar.getInstance();
				int minute = cal.get(Calendar.MINUTE);
				Date yesterday = TaskHelper.yesterdayZero(new Date());

				Transaction t = Cat.newTransaction("System", "CreateTask");
				try {
					// Daily report should created after last day reports all insert to database
					if (minute > 6) {
						creatReportTask(yesterday);
					} else {
						try {
							Thread.sleep((7 - minute) * TimeUtil.ONE_MINUTE);
						} catch (InterruptedException e) {
							active = false;
						}
						creatReportTask(yesterday);
					}
					t.setStatus(Transaction.SUCCESS);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				} finally {
					t.complete();
				}
				m_currentDay = currentDay.getTime();
			}
			try {
				Thread.sleep(5 * TimeUtil.ONE_MINUTE);
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}
}
