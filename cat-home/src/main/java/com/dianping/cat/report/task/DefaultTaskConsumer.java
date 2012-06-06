/**
 * 
 */
package com.dianping.cat.report.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.xml.sax.SAXException;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
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
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since May 28, 2012
 */
public class DefaultTaskConsumer extends TaskConsumer implements LogEnabled {

	class GraphLine {
		long totalCount;

		long failCount;

		double min;

		double max;

		double sum;

		double sum2;
	}

	@Inject
	private TaskDao taskDao;

	@Inject
	private GraphDao graphDao;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private DailygraphDao m_dailyGraphDao;

	@Inject
	private DailyreportDao m_dailyReportDao;

	private Logger m_logger;

	private com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser transactionParser = new com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser();

	private com.dianping.cat.consumer.event.model.transform.DefaultDomParser eventParser = new com.dianping.cat.consumer.event.model.transform.DefaultDomParser();

	private com.dianping.cat.consumer.heartbeat.model.transform.DefaultDomParser heartbeatParser = new com.dianping.cat.consumer.heartbeat.model.transform.DefaultDomParser();

	private com.dianping.cat.consumer.problem.model.transform.DefaultDomParser problemParser = new com.dianping.cat.consumer.problem.model.transform.DefaultDomParser();

	public DefaultTaskConsumer() {
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#findDoingTask()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#findTodoTask()
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#mergeYesterdayReport()
	 */
	@Override
	protected void mergeReport(Task task) {
		String reportName = task.getReportName();
		String reportDomain = task.getReportDomain();
		Date reportPeriod = task.getReportPeriod();
		Date yesterdayZero = TaskHelper.yesterdayZero(reportPeriod);
		Date todayZero = TaskHelper.todayZero(reportPeriod);
		try {
			Dailyreport report = m_dailyReportDao.findByNameDomainPeriod(yesterdayZero, reportDomain, reportName, DailyreportEntity.READSET_FULL);
			if (report != null) {
				return;
			}
		} catch (DalException e) {
			m_logger.info("no daily report");
		}

		m_logger.info(String.format("start merge %s report:%s from %s to %s: ", reportName, reportDomain, yesterdayZero, todayZero));

		String content = null;
		try {
			List<Report> reports = m_reportDao.findAllByDomainNameDuration(yesterdayZero, todayZero, reportDomain, reportName, ReportEntity.READSET_FULL);
			if ("transaction".equals(reportName)) {
				TransactionReport transactionReport = mergeTransactionReports(reportDomain, reports);
				TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportDomain));
				TransactionReport transactionReport2 = mergeTransactionReports(reportDomain, reports);
				com.dianping.cat.consumer.transaction.model.entity.Machine allMachines = merger.mergesForAllMachine(transactionReport2);
				transactionReport.addMachine(allMachines);
				transactionReport.getIps().add("All");
				content = transactionReport.toString();
			} else if ("event".equals(reportName)) {
				EventReport eventReport = mergeEventReports(reportDomain, reports);
				EventReportMerger merger = new EventReportMerger(new EventReport(reportDomain));
				EventReport eventReport2 = mergeEventReports(reportDomain, reports);
				com.dianping.cat.consumer.event.model.entity.Machine allMachines = merger.mergesForAllMachine(eventReport2);
				eventReport.addMachine(allMachines);
				eventReport.getIps().add("All");
				content = eventReport.toString();
			} else if ("heartbeat".equals(reportName)) {
				HeartbeatReport heartbeatReport = mergeHeartbeatReports(reportDomain, reports);
				content = heartbeatReport.toString();
			} else if ("problem".equals(reportName)) {
				ProblemReport problemReport = mergeProblemReports(reportDomain, reports);
				content = problemReport.toString();
			}
			Dailyreport report = m_dailyReportDao.createLocal();
			report.setContent(content);
			report.setCreationDate(new Date());
			report.setDomain(reportDomain);
			report.setIp(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			report.setName(reportName);
			report.setPeriod(yesterdayZero);
			report.setType(1);

			m_dailyReportDao.insert(report);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#doTodoTask(com.dianping.cat.hadoop.dal.Task)
	 */
	@Override
	protected boolean processTask(Task doing) {
		String reportName = doing.getReportName();
		String reportDomain = doing.getReportDomain();
		Date reportPeriod = doing.getReportPeriod();

		m_logger.info(String.format("start proecess %s task %s in %s: ", reportDomain, reportName, reportPeriod));

		try {
			List<Graph> graphs = null;
			List<Report> reports = m_reportDao.findAllByPeriodDomainName(reportPeriod, reportDomain, reportName, ReportEntity.READSET_FULL);

			if ("transaction".equals(reportName)) {
				TransactionReport transactionReport = mergeTransactionReports(reportDomain, reports);
				graphs = splitTransactionReportToGraphs(reportPeriod, reportDomain, reportName, transactionReport);
			} else if ("event".equals(reportName)) {
				EventReport eventReport = mergeEventReports(reportDomain, reports);
				graphs = splitEventReportToGraphs(reportPeriod, reportDomain, reportName, eventReport);
			} else if ("heartbeat".equals(reportName)) {
				HeartbeatReport heartbeatReport = mergeHeartbeatReports(reportDomain, reports);
				graphs = splitHeartbeatReportToGraphs(reportPeriod, reportDomain, reportName, heartbeatReport);
			} else if ("problem".equals(reportName)) {
				ProblemReport problemReport = mergeProblemReports(reportDomain, reports);
				graphs = splitProblemReportToGraphs(reportPeriod, reportDomain, reportName, problemReport);
			}

			if (graphs != null) {
				for (Graph graph : graphs) {
					this.graphDao.insert(graph); // use mysql unique index and insert on duplicate
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private ProblemReport mergeProblemReports(String reportDomain, List<Report> reports) throws SAXException, IOException {
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			ProblemReport model = problemParser.parse(xml);
			model.accept(merger);
		}

		ProblemReport problemReport = merger == null ? null : merger.getProblemReport();
		return problemReport;
	}

	private HeartbeatReport mergeHeartbeatReports(String reportDomain, List<Report> reports) throws SAXException, IOException {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			HeartbeatReport model = heartbeatParser.parse(xml);
			model.accept(merger);
		}

		HeartbeatReport heartbeatReport = merger == null ? null : merger.getHeartbeatReport();
		return heartbeatReport;
	}

	private EventReport mergeEventReports(String reportDomain, List<Report> reports) throws SAXException, IOException {
		EventReport eventReport;
		EventReportMerger merger = new EventReportMerger(new EventReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			EventReport model = eventParser.parse(xml);
			model.accept(merger);
		}

		eventReport = merger == null ? null : merger.getEventReport();
		return eventReport;
	}

	private TransactionReport mergeTransactionReports(String reportDomain, List<Report> reports) throws SAXException, IOException {
		TransactionReport transactionReport;
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			TransactionReport model = transactionParser.parse(xml);
			model.accept(merger);
		}

		transactionReport = merger == null ? null : merger.getTransactionReport();
		return transactionReport;
	}

	private List<Graph> splitEventReportToGraphs(Date reportPeroid, String reportDomain, String reportName, EventReport transactionReport) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Graph> splitHeartbeatReportToGraphs(Date reportPeroid, String reportDomain, String reportName, HeartbeatReport heartbeatReport) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Graph> splitProblemReportToGraphs(Date reportPeroid, String reportDomain, String reportName, ProblemReport problemReport) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<Graph> splitTransactionReportToGraphs(Date reportPeroid, String domainName, String reportName, TransactionReport transactionReport) {
		Set<String> ips = transactionReport.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1); // all and every machine
		Map<String, GraphLine> allGraphCache = new HashMap<String, GraphLine>();
		Date creationDate = new Date();
		for (String ip : ips) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(domainName);
			graph.setName(reportName);
			graph.setPeriod(reportPeroid);
			graph.setType(3);
			graph.setCreationDate(creationDate);
			Machine machine = transactionReport.getMachines().get(ip);
			Map<String, TransactionType> types = machine.getTypes();
			StringBuilder contentBuilder = new StringBuilder();
			for (Entry<String, TransactionType> transactionEntry : types.entrySet()) {
				TransactionType transactionType = transactionEntry.getValue();
				Map<String, TransactionName> names = transactionType.getNames();
				for (Entry<String, TransactionName> nameEntry : names.entrySet()) {
					TransactionName transactionName = nameEntry.getValue();
					contentBuilder.append(transactionType.getId());
					contentBuilder.append('\t');
					contentBuilder.append(transactionName.getId());
					contentBuilder.append('\t');
					contentBuilder.append(transactionName.getTotalCount());
					contentBuilder.append('\t');
					contentBuilder.append(transactionName.getFailCount());
					contentBuilder.append('\t');
					contentBuilder.append(transactionName.getMin());
					contentBuilder.append('\t');
					contentBuilder.append(transactionName.getMax());
					contentBuilder.append('\t');
					contentBuilder.append(transactionName.getSum());
					contentBuilder.append('\t');
					contentBuilder.append(transactionName.getSum2());
					contentBuilder.append('\n');

					String key = transactionType.getId() + "\t" + transactionName.getId();
					GraphLine graphLine = allGraphCache.get(key);
					if (graphLine == null) {
						graphLine = new GraphLine();
						allGraphCache.put(key, graphLine);
					}

					graphLine.totalCount += transactionName.getTotalCount();
					graphLine.failCount += transactionName.getFailCount();
					graphLine.min += transactionName.getMin();
					graphLine.max += transactionName.getMax();
					graphLine.sum += transactionName.getSum();
					graphLine.sum2 += transactionName.getSum2();
				}
			}
			graph.setContent(contentBuilder.toString());
			graphs.add(graph);
		}

		Graph allGraph = new Graph();
		allGraph.setIp(null);
		allGraph.setDomain(domainName);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeroid);
		allGraph.setType(3);
		allGraph.setCreationDate(creationDate);

		StringBuilder sb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allGraphCache.entrySet()) {
			sb.append(entry.getKey());
			sb.append('\t');
			GraphLine value = entry.getValue();
			sb.append(value.totalCount);
			sb.append('\t');
			sb.append(value.failCount);
			sb.append('\t');
			sb.append(value.min);
			sb.append('\t');
			sb.append(value.max);
			sb.append('\t');
			sb.append(value.sum);
			sb.append('\t');
			sb.append(value.sum2);
			sb.append('\n');
		}
		allGraph.setContent(sb.toString());

		graphs.add(allGraph);

		return graphs;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#taskNotFindSleep()
	 */
	@Override
	protected void taskNotFoundDuration() {
		Date awakeTime = TaskHelper.nextTaskTime();
		m_logger.info("waiting for next task until: " + awakeTime);
		LockSupport.parkUntil(awakeTime.getTime());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#todoTaskFailSleep()
	 */
	@Override
	protected void taskRetryDuration(Task task, int retryTimes) {
		m_logger.warn("TaskConsumer retry " + retryTimes + ", " + task.toString());
		LockSupport.parkNanos(10L * 1000 * 1000 * 1000);// sleep 10 sec
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#changeDoingStatus(com.dianping.cat.hadoop.dal.Task)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#failTodoTask(com.dianping.cat.hadoop.dal.Task)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#updateTodoStatus(com.dianping.cat.hadoop.dal.Task)
	 */
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
