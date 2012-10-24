package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.ReportEntity;
import com.dainping.cat.consumer.dal.report.Task;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.home.dal.report.Dailyreport;
import com.dianping.cat.home.dal.report.DailyreportDao;
import com.dianping.cat.home.dal.report.DailyreportEntity;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.site.dal.jdbc.DalException;
import com.site.dal.jdbc.DalNotFoundException;
import com.site.lookup.annotation.Inject;

public class DailyTaskProducer implements com.site.helper.Threads.Task, Initializable {

	private static final long DAY = 24 * 60 * 60 * 1000L;

	private static final int TYPE_DAILY = 1;

	@Inject
	private DailyreportDao m_dailyReportDao;

	private Set<String> m_dailyReportNameSet = new HashSet<String>();

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private boolean checkDatabaseTaskGenerated(Date day) {
		List<Dailyreport> allReports = new ArrayList<Dailyreport>();

		try {
			allReports = m_dailyReportDao.findDatabaseAllByPeriod(day, new Date(day.getTime() + DAY),
			      DailyreportEntity.READSET_DOMAIN_NAME);
		} catch (DalNotFoundException notFoundException) {
		} catch (DalException e) {
			Cat.logError(e);
		}

		Set<String> databaseSet = getDatabaseSet(day, new Date(day.getTime() + DAY));
		int total = allReports.size();

		if (total != databaseSet.size()) {
			return false;
		}
		return true;
	}

	private boolean checkDomainTaskGenerated(Date day) {
		List<Dailyreport> allReports = new ArrayList<Dailyreport>();

		try {
			allReports = m_dailyReportDao.findAllByPeriod(day, new Date(day.getTime() + DAY),
			      DailyreportEntity.READSET_DOMAIN_NAME);
		} catch (DalNotFoundException notFoundException) {
		} catch (DalException e) {
			Cat.logError(e);
		}

		Set<String> domainSet = getDomainSet(day, new Date(day.getTime() + DAY));
		int domanSize = domainSet.size();
		int nameSize = m_dailyReportNameSet.size();
		int total = allReports.size();

		if (total != domanSize * nameSize) {
			return false;
		}
		return true;
	}

	private void generateDatabaseTasks(Date day) {
		Transaction t = Cat.newTransaction("System", "ProduceDatabaseReport");
		try {
			Set<String> databaseSet = getDatabaseSet(day, new Date(day.getTime() + DAY));

			for (String domain : databaseSet) {
				Task task = m_taskDao.createLocal();

				task.setCreationDate(new Date());
				task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
				task.setReportDomain(domain);
				task.setReportName("database");
				task.setReportPeriod(day);
				task.setStatus(1);
				task.setTaskType(TYPE_DAILY);
				try {
					m_taskDao.insert(task);
				} catch (DalException e) {
					Cat.logError(e);
					t.setStatus(e);
				}
			}
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	private void generateDomainDailyTasks(Date day) {
		Transaction t = Cat.newTransaction("System", "ProduceDailyReport");
		try {
			Set<String> domainSet = getDomainSet(day, new Date(day.getTime() + DAY));

			for (String domain : domainSet) {
				for (String name : m_dailyReportNameSet) {
					Task task = m_taskDao.createLocal();

					task.setCreationDate(new Date());
					task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
					task.setReportDomain(domain);
					task.setReportName(name);
					task.setReportPeriod(day);
					task.setStatus(1);
					task.setTaskType(TYPE_DAILY);
					try {
						m_taskDao.insert(task);
					} catch (DalException e) {
						Cat.logError(e);
						t.setStatus(e);
					}
				}
			}
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
	}

	private Set<String> getDatabaseSet(Date start, Date end) {
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

	private Set<String> getDomainSet(Date start, Date end) {
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
	public String getName() {
		return "DailyTask-Producer";
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
	}

	@Override
	public void run() {
		boolean active = true;
		
		while (active) {
			try {
				Calendar cal = Calendar.getInstance();
				int minute = cal.get(Calendar.MINUTE);

				// Daily report should created after last day reports all insert to database
				if (minute > 10) {
					Date yestoday = TaskHelper.yesterdayZero(new Date());
					if (!checkDomainTaskGenerated(yestoday)) {
						generateDomainDailyTasks(yestoday);
					}
					if (!checkDatabaseTaskGenerated(yestoday)) {
						generateDatabaseTasks(yestoday);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			try {
				Thread.sleep(10 * 60 * 1000);
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}
}
