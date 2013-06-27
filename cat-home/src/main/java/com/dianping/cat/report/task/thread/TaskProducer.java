package com.dianping.cat.report.task.thread;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.core.dal.Task;
import com.dianping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.consumer.core.dal.TaskEntity;
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

	private void creatReportTask(Date yesterday) {
		Date lastWeekEnd = TimeUtil.getCurrentWeek();
		Date lastWeekStart = TimeUtil.getLastWeek();
		Date currentMonth = TimeUtil.getCurrentMonth();
		Date lastMonth = TimeUtil.getLastMonth();

		generateDailyReportTasks(yesterday);
		generateWeeklyReportTasks(lastWeekStart, lastWeekEnd);
		generateMonthReportTasks(lastMonth, currentMonth);
	}

	private void generateDailyReportTasks(Date date) {
		try {
			Set<String> domainSet = queryDomainSet(date, new Date(date.getTime() + TimeUtil.ONE_DAY));

			for (String domain : domainSet) {
				for (String name : m_dailyReportNameSet) {
					try {
						m_taskDao.findByDomainNameTypePeriod(name, domain, ReportFacade.TYPE_DAILY, date,
						      TaskEntity.READSET_FULL);
					} catch (DalNotFoundException e) {
						insertTask(domain, name, date, ReportFacade.TYPE_DAILY);
					}
				}
			}
			try {
				m_taskDao.findByDomainNameTypePeriod(STATE, CatString.CAT, ReportFacade.TYPE_DAILY, date,
				      TaskEntity.READSET_FULL);
			} catch (DalNotFoundException e) {
				insertTask(CatString.CAT, STATE, date, ReportFacade.TYPE_DAILY);
			} catch (DalException e) {
				Cat.logError(e);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void generateMonthReportTasks(Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			Date date = new Date(startTime);
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);

			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
			if (dayOfMonth == 1) {
				for (String name : m_dailyReportNameSet) {
					Calendar monthEnd = Calendar.getInstance();

					monthEnd.setTime(date);
					monthEnd.add(Calendar.MONTH, 1);

					Set<String> domainSet = queryDomainSet(date, monthEnd.getTime());
					for (String domain : domainSet) {
						try {
							m_taskDao.findByDomainNameTypePeriod(name, domain, ReportFacade.TYPE_MONTH, date,
							      TaskEntity.READSET_FULL);
						} catch (DalNotFoundException e) {
							insertTask(domain, name, date, ReportFacade.TYPE_MONTH);
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				}

				try {
					try {
						m_taskDao.findByDomainNameTypePeriod(STATE, CatString.CAT, ReportFacade.TYPE_WEEK, date,
						      TaskEntity.READSET_FULL);
					} catch (DalNotFoundException e) {
						insertTask(CatString.CAT, STATE, date, ReportFacade.TYPE_MONTH);
					}
				} catch (DalException e) {
					Cat.logError(e);
				}
			}
		}
	}

	private void generateWeeklyReportTasks(Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			Date date = new Date(startTime);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == 7) {
				for (String name : m_dailyReportNameSet) {
					Set<String> domainSet = queryDomainSet(date, new Date(date.getTime() + TimeUtil.ONE_DAY * 7));
					for (String domain : domainSet) {
						try {
							m_taskDao.findByDomainNameTypePeriod(name, domain, ReportFacade.TYPE_WEEK, date,
							      TaskEntity.READSET_FULL);
						} catch (DalNotFoundException e) {
							insertTask(domain, name, date, ReportFacade.TYPE_WEEK);
						} catch (Exception e) {
							Cat.logError(e);
						}
					}
				}
				try {
					try {
						m_taskDao.findByDomainNameTypePeriod(STATE, CatString.CAT, ReportFacade.TYPE_WEEK, date,
						      TaskEntity.READSET_FULL);
					} catch (DalNotFoundException e) {
						insertTask(CatString.CAT, STATE, date, ReportFacade.TYPE_WEEK);
					}
				} catch (DalException e) {
					Cat.logError(e);
				}
			}
		}
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

	private void insertTask(String domain, String name, Date date, int type) {
		Task task = m_taskDao.createLocal();

		task.setCreationDate(new Date());
		task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		task.setReportDomain(domain);
		task.setReportName(name);
		task.setReportPeriod(date);
		task.setStatus(1);
		task.setTaskType(type);

		try {
			m_taskDao.insert(task);
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
