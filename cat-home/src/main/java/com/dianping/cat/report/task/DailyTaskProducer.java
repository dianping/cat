package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

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

	private static final int TYPE_DAILY = 1;

	private static final long DAY = 24 * 60 * 60 * 1000L;

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
		while (true) {
			Date now = new Date();
			Date todayStart = TaskHelper.todayZero(now);
			Date todayEnd = TaskHelper.tomorrowZero(now);
			Date startDateOfNextTask = TaskHelper.startDateOfNextTask(now);

			LockSupport.parkUntil(startDateOfNextTask.getTime());
			if (!checkTaskGenerated(todayStart)) {
				generateDailyTasks(todayStart, todayEnd);
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		Date now = new Date();
		Date yesterdayStart = TaskHelper.yesterdayZero(now);
		Date yesterdayEnd = TaskHelper.todayZero(now);

		if (!checkTaskGenerated(yesterdayStart)) {
			generateDailyTasks(yesterdayStart, yesterdayEnd);
		}
	}

	private void generateDailyTasks(Date start, Date end) {
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

	private boolean checkTaskGenerated(Date start) {
		List<Dailyreport> allReports = new ArrayList<Dailyreport>();
		try {
			allReports = m_dailyReportDao.findAllByPeriod(start, new Date(start.getTime() + DAY),
			      DailyreportEntity.READSET_COUNT);
		} catch (DalException e) {
			m_logger.error("DailyTaskProducer isYesterdayTaskGenerated", e);
		}

		Set<String> domainSet = getDomainSet(start, new Date(start.getTime() + DAY));

		int total = 0;
		int domanSize = domainSet.size();
		int nameSize = m_dailyReportNameSet.size();
		
		//SQL Framework
		if (allReports != null && allReports.size() > 0) {
			total = allReports.get(0).getCount();
		}

		if (total != domanSize * nameSize) {
			return false;
		}
		return true;
	}
}
