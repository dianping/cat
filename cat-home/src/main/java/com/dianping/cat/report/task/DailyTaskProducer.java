package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class DailyTaskProducer implements Runnable, Initializable {

	private static final long DAY = 24 * 60 * 60 * 1000L;

	private static final int TYPE_DAILY = 1;

	@Inject
	private DailyreportDao m_dailyReportDao;

	private Set<String> m_dailyReportNameSet = new HashSet<String>();

	private Logger m_logger;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private boolean checkDatabaseTaskGenerated(Date day) {
		List<Dailyreport> allReports = new ArrayList<Dailyreport>();

		try {
			allReports = m_dailyReportDao.findDatabaseAllByPeriod(day, new Date(day.getTime() + DAY),
			      DailyreportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			m_logger.warn("DailyTaskProducer isYesterdayTaskGenerated", e);
		}

		Set<String> databaseSet = getDatabaseSet(day, new Date(day.getTime() + DAY));
		int total =  allReports.size();

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
		} catch (DalException e) {
			m_logger.warn("DailyTaskProducer isYesterdayTaskGenerated", e);
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
	public void initialize() throws InitializationException {
		m_dailyReportNameSet.add("event");
		m_dailyReportNameSet.add("transaction");
		m_dailyReportNameSet.add("problem");
		m_dailyReportNameSet.add("matrix");
		m_dailyReportNameSet.add("cross");
		m_dailyReportNameSet.add("sql");
	}

	@Override
	public void run() {
		while (true) {
			try {
				Date yestoday = TaskHelper.yesterdayZero(new Date());
				if (!checkDomainTaskGenerated(yestoday)) {
					generateDomainDailyTasks(yestoday);
				}
				if (!checkDatabaseTaskGenerated(yestoday)) {
					generateDatabaseTasks(yestoday);
				}
				Thread.sleep(10 * 60 * 1000);
			} catch (Exception e) {
			}
		}
	}
}
