/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.concurrent.locks.LockSupport;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.dal.TaskEntity;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since May 28, 2012
 */
public class DefaultTaskConsumer extends TaskConsumer {

	@Inject
	private TaskDao taskDao;

	@Inject
	private CatReportFacade reportFacade;

	public DefaultTaskConsumer() {
	}

	@Override
	protected Task findDoingTask(String ip) {
		Task task = null;
		try {
			task = this.taskDao.findByStatusConsumer(STATUS_DOING, ip, TaskEntity.READSET_FULL);
		} catch (DalException e) {
<<<<<<< HEAD
=======
			//TODO
			//m_logger.info("no doing task");
>>>>>>> 34aa1347a27cbd2b5539cbcf2043a0c7acc392b2
		}
		return task;
	}

	@Override
	protected Task findTodoTask() {
		Task task = null;
		try {
			task = this.taskDao.findByStatusConsumer(STATUS_TODO, null, TaskEntity.READSET_FULL);
		} catch (DalException e) {
<<<<<<< HEAD
=======
			//TODO
			//m_logger.info("no todo task");
>>>>>>> 34aa1347a27cbd2b5539cbcf2043a0c7acc392b2
		}
		return task;
	}

	@Override
<<<<<<< HEAD
=======
	protected void mergeReport(Task task) {
		Date reportPeriod = task.getReportPeriod();
		Date startDate = TaskHelper.yesterdayZero(reportPeriod);
		Date endDate = TaskHelper.todayZero(reportPeriod);

		List<Report> domainNames = null;
		try {
			domainNames = m_reportDao.findAllByDomainNameDuration(startDate, endDate, null, null,
			      ReportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			m_logger.error("domainNames", e);
		}

		if (domainNames == null || domainNames.size() == 0) {
			return; // no hourly report
		}

		Set<String> domainSet = new HashSet<String>();
		Set<String> nameSet = new HashSet<String>();
		for (Report domainName : domainNames) {
			domainSet.add(domainName.getDomain());
			// ignore heartbeat and ip daily report merge
			if (!"heartbeat".equals(domainName.getName()) && !"ip".equals(domainName.getName())) {
				nameSet.add(domainName.getName());
			}
		}

		List<Dailyreport> dailyDomainNames = null;
		try {
			dailyDomainNames = m_dailyReportDao.findAllByPeriod(startDate, endDate, DailyreportEntity.READSET_DOMAIN_NAME);
		} catch (DalException e) {
			m_logger.error("dailyDomainNames", e);
		}

		Set<String> dailySet = new HashSet<String>();
		if (dailyDomainNames != null) {
			for (Dailyreport domainName : dailyDomainNames) {
				dailySet.add(domainName.getDomain() + "\t" + domainName.getName());
			}
		}
		for (String domain : domainSet) { // iterate domains
			for (String name : nameSet) { // iterate report names
				String key = domain + "\t" + name;
				if (dailySet.contains(key)) {
					continue; // ignore exist daily report
				}

				//m_logger.info(String.format("Starting merge domain:%s daily report:%s from %s to %s: ", domain, name,
				//      startDate, endDate));

				mergeDomainDailyReport(domain, name, startDate, endDate, domainSet);

				//m_logger.info(String.format("finish merge domain:%s daily report:%s from %s to %s: ", domain, name,
				//      startDate, endDate));
			}
		}
	}

	private TransactionMerger m_transactionMerger = new TransactionMerger();

	private EventMerger m_eventMerger = new EventMerger();

	private ProblemMerger m_problemMerger = new ProblemMerger();

	private HeartbeatMerger m_heartbeatMerger = new HeartbeatMerger();

	private void mergeDomainDailyReport(String reportDomain, String reportName, Date startDate, Date endDate,
	      Set<String> domainSet) {
		String content = null;
		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(startDate, endDate, reportDomain, reportName,
			      ReportEntity.READSET_FULL);
			if ("transaction".equals(reportName)) {
				content = m_transactionMerger.mergeForDaily(reportDomain, reports, domainSet).toString();
			} else if ("event".equals(reportName)) {
				content = m_eventMerger.mergeForDaily(reportDomain, reports, domainSet).toString();
			} else if ("problem".equals(reportName)) {
				content = m_problemMerger.mergeForDaily(reportDomain, reports, domainSet).toString();
			} else {
				return;
			}
			Dailyreport report = m_dailyReportDao.createLocal();
			report.setContent(content);
			report.setCreationDate(new Date());
			report.setDomain(reportDomain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(reportName);
			report.setPeriod(startDate);
			report.setType(1);

			m_dailyReportDao.insert(report);

		} catch (Exception e) {
			m_logger.error("mergeDomainDailyReport", e);
		}
	}

	private TransactionGraphCreator m_transactionGraphCreator = new TransactionGraphCreator();

	private EventGraphCreator m_eventGraphCreator = new EventGraphCreator();

	private HeartbeatGraphCreator m_heartbeatGraphCreator = new HeartbeatGraphCreator();

	private ProblemGraphCreator m_problemGraphCreator = new ProblemGraphCreator();

	@Override
>>>>>>> 34aa1347a27cbd2b5539cbcf2043a0c7acc392b2
	protected boolean processTask(Task doing) {
		return reportFacade.builderReport(doing);
	}

	@Override
	protected void taskNotFoundDuration() {
		Date awakeTime = TaskHelper.nextTaskTime();
		LockSupport.parkUntil(awakeTime.getTime());
	}

	@Override
	protected void taskRetryDuration(Task task, int retryTimes) {
		LockSupport.parkNanos(10L * 1000 * 1000 * 1000);// sleep 10 sec
	}

	@Override
	protected boolean updateDoingToDone(Task doing) {
		doing.setStatus(STATUS_DONE);
		doing.setEndDate(new Date());

		try {
			return this.taskDao.updateDoingToDone(doing, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	protected boolean updateDoingToFailure(Task doing) {
		doing.setStatus(STATUS_FAIL);
		doing.setEndDate(new Date());

		try {
			return this.taskDao.updateDoingToFail(doing, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	protected boolean updateTodoToDoing(Task todo) {
		todo.setStatus(STATUS_DOING);
		todo.setConsumer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		todo.setStartDate(new Date());

		try {
			return this.taskDao.updateTodoToDoing(todo, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			e.printStackTrace();
			return false;
		}
	}

}
