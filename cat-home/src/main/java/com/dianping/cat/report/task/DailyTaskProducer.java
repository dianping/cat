package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
import com.dianping.cat.message.Transaction;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

public class DailyTaskProducer implements Runnable, Initializable {

	private static final int TYPE_DAILY = 1;

	private static final int NUM_OF_THREADS = 2;

	private static final int PERIOD = 24 * 60 * 60 * 1000;

	private final Set<String> m_dailyReportNameSet = new HashSet<String>();

	@Inject
	private TaskDao m_taskDao;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private DailyreportDao m_dailyReportDao;

	private Logger m_logger;

	private ScheduledExecutorService m_service = Executors.newScheduledThreadPool(NUM_OF_THREADS);

	@Override
	public void run() {
		// schedule a task at next day 00:09
		Date now = new Date();
		Date today = TaskHelper.todayZero(now);
		Date tomorrow = TaskHelper.tomorrowZero(now);
		DailyTask dailyTask = new DailyTask(today, tomorrow);
		Date startDateOfNextTask = TaskHelper.startDateOfNextTask(now, 1);
		long delay = startDateOfNextTask.getTime() - now.getTime();

		m_service.scheduleAtFixedRate(dailyTask, delay, PERIOD, TimeUnit.MILLISECONDS);
	}

	private class DailyTask implements Runnable {

		private Date m_start;

		private Date m_end;

		public DailyTask(Date start, Date end) {
			m_start = start;
			m_end = end;
		}

		public void run() {
			Transaction t = Cat.newTransaction("System", "ProduceDailyReport");
			try {
				Set<String> domainSet = new HashSet<String>();
				Set<String> nameSet = new HashSet<String>();

				getDomainAndNameSet(domainSet, nameSet, m_start, m_end);
				nameSet.retainAll(m_dailyReportNameSet);

				for (String domain : domainSet) { // iterate domains
					for (String name : nameSet) {
						Task task = m_taskDao.createLocal();
						task.setCreationDate(new Date());
						task.setProducer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
						task.setReportDomain(domain);
						task.setReportName(name);
						task.setReportPeriod(m_start);
						task.setStatus(1); // status todo
						task.setTaskType(TYPE_DAILY);
						try {
							m_taskDao.insert(task);
						} catch (DalException e) {
							Cat.logError(e);
							t.setStatus(e);
						}
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
				t.setStatus(e);
			} finally {
				t.complete();
			}
		}
	}

	private void getDomainAndNameSet(Set<String> domainSet, Set<String> nameSet, Date start, Date end) {
		List<Report> domainNames = new ArrayList<Report>();
		try {
			domainNames = m_reportDao
			      .findAllByDomainNameDuration(start, end, null, null, ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			Cat.logError(e);
		}

		if (domainNames == null || domainNames.size() == 0) {
			return;
		}

		for (Report domainName : domainNames) {
			domainSet.add(domainName.getDomain());
			// ignore heartbeat and ip daily report merge
			if (!"heartbeat".equals(domainName.getName()) && !"ip".equals(domainName.getName())) {
				nameSet.add(domainName.getName());
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Date now = new Date();
		Date todayZero = TaskHelper.todayZero(now);
		Date yesterday = TaskHelper.yesterdayZero(now);

		if (!isYesterdayTaskGenerated(now, todayZero, yesterday)) {
			DailyTask dailyTask = new DailyTask(yesterday, todayZero);
			long startOfTask = TaskHelper.startDateOfNextTask(now, 0).getTime();
			long delay = startOfTask - now.getTime();
			
			m_service.schedule(dailyTask, delay, TimeUnit.MILLISECONDS);
		}

		m_dailyReportNameSet.add("event");
		m_dailyReportNameSet.add("transaction");
		m_dailyReportNameSet.add("problem");
	}

	private boolean isYesterdayTaskGenerated(Date now, Date todayZero, Date yesterdayZero) {
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

			Set<String> domainSet = new HashSet<String>();
			Set<String> nameSet = new HashSet<String>();

			getDomainAndNameSet(domainSet, nameSet, yesterdayZero, todayZero);
			nameSet.retainAll(m_dailyReportNameSet);

			int total = allReports.get(0).getCount();
			int domanSize = domainSet.size();
			int nameSize = nameSet.size();

			if (total != domanSize * nameSize) {
				return false;
			}
		}
		return true;
	}
}
