/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.hadoop.dal.DailygraphDao;
import com.dianping.cat.hadoop.dal.Dailyreport;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.DailyreportEntity;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.GraphDao;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.dal.TaskEntity;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since May 28, 2012
 */
public class DefaultTaskConsumer extends TaskConsumer implements LogEnabled {

	@Inject
	private TaskDao taskDao;

	@Inject
	private GraphDao graphDao;

	@Inject
	private ReportDao m_reportDao;

	@SuppressWarnings("unused")
	@Inject
	private DailygraphDao m_dailyGraphDao;

	@Inject
	private DailyreportDao m_dailyReportDao;

	private Logger m_logger;

	public DefaultTaskConsumer() {
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	protected Task findDoingTask(String ip) {
		Task task = null;
		try {
			task = this.taskDao.findByStatusConsumer(STATUS_DOING, ip, TaskEntity.READSET_FULL);
		} catch (DalException e) {
			m_logger.info("no doing task");
		}
		return task;
	}

	@Override
	protected Task findTodoTask() {
		Task task = null;
		try {
			task = this.taskDao.findByStatusConsumer(STATUS_TODO, null, TaskEntity.READSET_FULL);
		} catch (DalException e) {
			m_logger.info("no todo task");
		}
		return task;
	}

	@Override
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
		for (Dailyreport domainName : dailyDomainNames) {
			dailySet.add(domainName.getDomain() + "\t" + domainName.getName());
		}

		for (String domain : domainSet) { // iterate domains
			for (String name : nameSet) { // iterate report names
				String key = domain + "\t" + name;
				if (dailySet.contains(key)) {
					continue; // ignore exist daily report
				}

				m_logger.info(String.format("Starting merge domain:%s daily report:%s from %s to %s: ", domain, name,
				      startDate, endDate));

				mergeDomainDailyReport(domain, name, startDate, endDate, domainSet);

				m_logger.info(String.format("finish merge domain:%s daily report:%s from %s to %s: ", domain, name,
				      startDate, endDate));
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
				content = m_transactionMerger.mergeAll(reportDomain, reports, domainSet);
			} else if ("event".equals(reportName)) {
				content = m_eventMerger.mergeAll(reportDomain, reports, domainSet);
			} else if ("heartbeat".equals(reportName)) {
				// do nothing
				return;
			} else if ("problem".equals(reportName)) {
				content = m_problemMerger.mergeAll(reportDomain, reports, domainSet);
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

			System.out.println(report.getDomain() + " >>>>>>>>" + report.getName());
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
	protected boolean processTask(Task doing) {
		String reportName = doing.getReportName();
		String reportDomain = doing.getReportDomain();
		Date reportPeriod = doing.getReportPeriod();

		m_logger.info(String.format("start proecess %s task %s in %s: ", reportDomain, reportName, reportPeriod));

		try {
			List<Graph> graphs = null;
			List<Report> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName,
			      ReportEntity.READSET_FULL);

			if ("transaction".equals(reportName)) {
				TransactionReport transactionReport = m_transactionMerger.merge(reportDomain, reports);
				graphs = m_transactionGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName,
				      transactionReport);
			} else if ("event".equals(reportName)) {
				EventReport eventReport = m_eventMerger.merge(reportDomain, reports);
				graphs = m_eventGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, eventReport);
			} else if ("heartbeat".equals(reportName)) {
				HeartbeatReport heartbeatReport = m_heartbeatMerger.merge(reportDomain, reports);
				graphs = m_heartbeatGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName,
				      heartbeatReport);
			} else if ("problem".equals(reportName)) {
				ProblemReport problemReport = m_problemMerger.merge(reportDomain, reports);
				graphs = m_problemGraphCreator.splitReportToGraphs(reportPeriod, reportDomain, reportName, problemReport);
			}

			if (graphs != null) {
				for (Graph graph : graphs) {
					this.graphDao.insert(graph); // use mysql unique index and insert
					                             // on duplicate
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	protected void taskNotFoundDuration() {
		Date awakeTime = TaskHelper.nextTaskTime();
		m_logger.info("waiting for next task until: " + awakeTime);
		LockSupport.parkUntil(awakeTime.getTime());
	}

	@Override
	protected void taskRetryDuration(Task task, int retryTimes) {
		m_logger.warn("TaskConsumer retry " + retryTimes + ", " + task.toString());
		LockSupport.parkNanos(10L * 1000 * 1000 * 1000);// sleep 10 sec
	}

	@Override
	protected boolean updateDoingToDone(Task doing) {
		doing.setStatus(STATUS_DONE);
		doing.setEndDate(new Date());

		m_logger.info("TaskConsumer doing to done, " + doing.toString());

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

		m_logger.error("TaskConsumer failed, " + doing.toString());

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

		m_logger.info("TaskConsumer todo to doing, " + todo.toString());

		try {
			return this.taskDao.updateTodoToDoing(todo, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			e.printStackTrace();
			return false;
		}
	}

}
