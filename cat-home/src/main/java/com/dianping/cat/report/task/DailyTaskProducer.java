package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.logging.Logger;

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

public class DailyTaskProducer implements Runnable {

	private volatile boolean m_stop = false;

	private boolean firstStart = true;

	private static final int TYPE_DAILY = 1;

	private static Set<String> m_dailyReportNameSet = new HashSet<String>();

	static {
		m_dailyReportNameSet.add("event");
		m_dailyReportNameSet.add("transaction");
		m_dailyReportNameSet.add("problem");
	}

	@Inject
	private TaskDao m_taskDao;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private DailyreportDao m_dailyReportDao;

	private Logger m_logger;

	@Override
	public void run() {
		while (!m_stop) {
			Date now = new Date();
			Date yesterdayZero = TaskHelper.yesterdayZero(now);
			Date todayZero = TaskHelper.todayZero(now);
			Date tomorrowZero = TaskHelper.tomorrowZero(now);

			if (firstStart) {
				if (!isYesterdayTaskGenerated(now, yesterdayZero, todayZero)) {
					generateDailyTasks(yesterdayZero, todayZero);
				}
				firstStart = false;
			}
			Date startDateOfNextTask = TaskHelper.startDateOfNextTask(now, 1);
			LockSupport.parkUntil(startDateOfNextTask.getTime());
			generateDailyTasks(todayZero, tomorrowZero);
		}
	}

	public void generateDailyTasks(Date start, Date end) {
		Transaction t = Cat.newTransaction("System", "ProduceDailyReport");
		try {
			Set<String> domainSet = getDomainSet(start, end);

			for (String domain : domainSet) {
				for (String name : m_dailyReportNameSet) {
					Task task = m_taskDao.createLocal();
					task.setCreationDate(new Date());
					task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
					task.setReportDomain(domain);
					task.setReportName(name);
					task.setReportPeriod(start);
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

	private boolean isYesterdayTaskGenerated(Date now, Date yesterdayZero, Date todayZero) {
		Date startDayOfTodayTask = TaskHelper.startDateOfNextTask(now, 0);
		long nowLong = now.getTime();
		long startOfTask = startDayOfTodayTask.getTime();

		if (nowLong <= startOfTask) {
			return false;
		}

		if (nowLong > startOfTask) {
			List<Dailyreport> allReports = new ArrayList<Dailyreport>();
			try {
				allReports = m_dailyReportDao.findAllByPeriod(yesterdayZero, todayZero, DailyreportEntity.READSET_COUNT);
			} catch (DalException e) {
				m_logger.error("DailyTask isYesterdayTaskGenerated", e);
			}

			Set<String> domainSet = getDomainSet(yesterdayZero, todayZero);

			int total = allReports.get(0).getCount();
			int domanSize = domainSet.size();
			int nameSize = m_dailyReportNameSet.size();

			if (total != domanSize * nameSize) {
				return false;
			}
		}
		return true;
	}

	public void stop() {
		m_stop = true;
	}
}
