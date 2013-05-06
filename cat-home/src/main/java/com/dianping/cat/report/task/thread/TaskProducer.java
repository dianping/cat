package com.dianping.cat.report.task.thread;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dainping.cat.consumer.core.dal.ReportEntity;
import com.dainping.cat.consumer.core.dal.Task;
import com.dainping.cat.consumer.core.dal.TaskDao;
import com.dainping.cat.consumer.core.dal.TaskEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportFacade;

public class TaskProducer implements org.unidal.helper.Threads.Task, Initializable {

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private Set<String> m_dailyReportNameSet = new HashSet<String>();

	private Set<String> m_graphReportNameSet = new HashSet<String>();

	private static final String STATE = "state";

	private long m_currentDay;

	private void creatReportTask(Date yesterday) {
		generateDailyReportTasks(yesterday);
		generateDailyDatabaseTasks(yesterday);

		generateDailyGraphTask(yesterday, TimeUtil.getCurrentDay());
		Date lastWeekEnd = TimeUtil.getCurrentWeek();
		Date lastWeekStart = TimeUtil.getLastWeek();

		generateWeeklyReportTasks(lastWeekStart, lastWeekEnd);
		generateWeeklyDatabaseReportTasks(lastWeekStart, lastWeekEnd);

		Date currentMonth = TimeUtil.getCurrentMonth();
		Date lastMonth = TimeUtil.getLastMonth();

		generateMonthReportTasks(lastMonth, currentMonth);
		generateMonthDatabaseReportTasks(lastMonth, currentMonth);
	}

	public void firstInit() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -3);
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 0, 0, 0, 0);
		cal.set(Calendar.MILLISECOND, 0);

		Date currentMonth = TimeUtil.getCurrentMonth();
		Date lastWeekEnd = TimeUtil.getCurrentWeek();

		generateWeeklyReportTasks(cal.getTime(), lastWeekEnd);
		generateWeeklyDatabaseReportTasks(cal.getTime(), lastWeekEnd);

		generateMonthReportTasks(cal.getTime(), currentMonth);
		generateMonthDatabaseReportTasks(cal.getTime(), currentMonth);

		Date yesterday = TimeUtil.getYesterday();
		generateDailyGraphTask(cal.getTime(), yesterday);
	}

	private void generateDailyDatabaseTasks(Date date) {
		try {
			Set<String> databaseSet = queryDatabaseSet(date, new Date(date.getTime() + TimeUtil.ONE_DAY));

			for (String domain : databaseSet) {
				try {
					m_taskDao.findByDomainNameTypePeriod("database", domain, ReportFacade.TYPE_DAILY, date,
					      TaskEntity.READSET_FULL);
				} catch (DalNotFoundException e) {
					insertTask(domain, "database", date, ReportFacade.TYPE_DAILY);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void generateDailyGraphTask(Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			Date date = new Date(startTime);
			Set<String> domainSet = queryDomainSet(date, new Date(date.getTime() + TimeUtil.ONE_DAY));

			for (String domain : domainSet) {
				for (String name : m_graphReportNameSet) {
					try {
						try {
							m_taskDao.findByDomainNameTypePeriod(name, domain, ReportFacade.TYPE_DAILY_GRAPH, date,
							      TaskEntity.READSET_FULL);
						} catch (DalNotFoundException e) {
							// insertTask(domain, name, date, ReportFacade.TYPE_DAILY_GRAPH);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}
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
				try {
					m_taskDao.findByDomainNameTypePeriod(STATE, CatString.CAT, ReportFacade.TYPE_DAILY, date,
					      TaskEntity.READSET_FULL);
				} catch (DalNotFoundException e) {
					insertTask(CatString.CAT, STATE, date, ReportFacade.TYPE_DAILY);
				}
			} catch (DalException e) {
				Cat.logError(e);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	private void generateMonthDatabaseReportTasks(Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			Date date = new Date(startTime);
			Calendar cal = Calendar.getInstance();

			cal.setTime(date);

			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
			if (dayOfMonth == 1) {
				Calendar monthEnd = Calendar.getInstance();

				monthEnd.setTime(date);
				monthEnd.add(Calendar.MONTH, 1);

				Set<String> databases = queryDatabaseSet(date, monthEnd.getTime());
				for (String database : databases) {
					try {
						try {
							m_taskDao.findByDomainNameTypePeriod("database", database, ReportFacade.TYPE_WEEK, date,
							      TaskEntity.READSET_FULL);
						} catch (DalNotFoundException e) {
							insertTask(database, "database", date, ReportFacade.TYPE_MONTH);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
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
							try {
								m_taskDao.findByDomainNameTypePeriod(name, domain, ReportFacade.TYPE_MONTH, date,
								      TaskEntity.READSET_FULL);
							} catch (DalNotFoundException e) {
								insertTask(domain, name, date, ReportFacade.TYPE_MONTH);
							}
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

	private void generateWeeklyDatabaseReportTasks(Date start, Date end) {
		long startTime = start.getTime();
		long endTime = end.getTime();

		for (; startTime < endTime; startTime += TimeUtil.ONE_DAY) {
			Date date = new Date(startTime);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			if (dayOfWeek == 7) {
				Set<String> databaseSet = queryDatabaseSet(date, new Date(date.getTime() + TimeUtil.ONE_DAY * 7));
				for (String database : databaseSet) {
					try {
						try {
							m_taskDao.findByDomainNameTypePeriod("database", database, ReportFacade.TYPE_WEEK, date,
							      TaskEntity.READSET_FULL);
						} catch (DalNotFoundException e) {
							insertTask(database, "database", date, ReportFacade.TYPE_WEEK);
						}
					} catch (Exception e) {
						Cat.logError(e);
					}
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
							try {
								m_taskDao.findByDomainNameTypePeriod(name, domain, ReportFacade.TYPE_WEEK, date,
								      TaskEntity.READSET_FULL);
							} catch (DalNotFoundException e) {
								insertTask(domain, name, date, ReportFacade.TYPE_WEEK);
							}
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
		m_dailyReportNameSet.add("event");
		m_dailyReportNameSet.add("transaction");
		m_dailyReportNameSet.add("problem");
		m_dailyReportNameSet.add("matrix");
		m_dailyReportNameSet.add("cross");
		m_dailyReportNameSet.add("sql");
		m_dailyReportNameSet.add("health");

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

	private Set<String> queryDatabaseSet(Date start, Date end) {
		List<Report> databaseNames = new ArrayList<Report>();
		Set<String> databaseSet = new HashSet<String>();

		try {
			databaseNames = m_reportDao.findDatabaseAllByDomainNameDuration(start, end, null, "database",
			      ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}

		if (databaseNames == null || databaseNames.size() == 0) {
			return databaseSet;
		}

		for (Report domainName : databaseNames) {
			databaseSet.add(domainName.getDomain());
		}
		return databaseSet;
	}

	private Set<String> queryDomainSet(Date start, Date end) {
		List<Report> domainNames = new ArrayList<Report>();
		Set<String> domainSet = new HashSet<String>();

		try {
			domainNames = m_reportDao
			      .findAllByDomainNameDuration(start, end, null, null, ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}

		if (domainNames == null || domainNames.size() == 0) {
			return domainSet;
		}

		for (Report domainName : domainNames) {
			domainSet.add(domainName.getDomain());
		}
		return domainSet;
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
