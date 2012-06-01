/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.mortbay.log.Log;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.dianping.cat.hadoop.dal.DailygraphDao;
import com.dianping.cat.hadoop.dal.DailyreportDao;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.hadoop.dal.GraphDao;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.dal.TaskEntity;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
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
	private ReportDao reportDao;

	@Inject
	private DailygraphDao dailyGraphDao;

	@Inject
	private DailyreportDao dailyReportDao;

	private long nextFindTimeMilli;

	public DefaultTaskConsumer() {
	}

	private Logger m_logger;

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
			task = this.taskDao.findByStatus(STATUS_DOING, ip, TaskEntity.READSET_FULL);
		} catch (DalException e) {
			Log.info("no doing task");
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
			task = this.taskDao.findByStatus(STATUS_TODO, null, TaskEntity.READSET_FULL);
		} catch (DalException e) {
			Log.info("no todo task");
		}
		return task;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#mergeYesterdayReport()
	 */
	@Override
	protected void mergeReport() {
		// TODO
	}

	private com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser transactionParser = new DefaultDomParser();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#doTodoTask(com.dianping.cat.hadoop.dal.Task)
	 */
	@Override
	protected boolean processTask(Task doing) {
		String reportName = doing.getReportName();
		String reportDomain = doing.getReportDomain();
		Date reportPeroid = doing.getReportPeriod();

		try {
			List<Graph> graphs = null;
			List<Report> reports = reportDao.findAllByPeriodDomainName(reportPeroid, reportDomain, reportName, ReportEntity.READSET_FULL);

			if ("transaction".equals(reportName)) {
				TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportDomain));

				for (Report report : reports) {
					String xml = report.getContent();
					TransactionReport model = transactionParser.parse(xml);
					model.accept(merger);
				}

				TransactionReport transactionReport = merger == null ? null : merger.getTransactionReport();

				graphs = splitTransactionReportToGraphs(reportPeroid, reportDomain, reportName, transactionReport);

			} else if ("event".equals(reportName)) {
				// TODO
			} else if ("heartbeat".equals(reportName)) {
				// TODO
			} else if ("problem".equals(reportName)) {
				// TODO
			}

			for (Graph graph : graphs) {
				this.graphDao.insert(graph);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	class GraphLine {
		long totalCount;

		long failCount;

		double min;

		double max;

		double sum;

		double sum2;
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
		allGraph.setIp(ips.iterator().next());
		allGraph.setDomain(domainName);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeroid);
		allGraph.setType(3);
		allGraph.setCreationDate(creationDate);

		graphs.add(allGraph);

		return graphs;
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
	 * @see com.dianping.cat.report.task.TaskConsumer#taskNotFindSleep()
	 */
	@Override
	protected void taskNotFoundDuration() {
		Calendar cal = Calendar.getInstance();
		int min = cal.get(Calendar.MINUTE);
		final int startFindMin = 10;
		cal.set(Calendar.MINUTE, startFindMin);
		cal.set(Calendar.SECOND, 0);
		if (min >= startFindMin) {
			cal.add(Calendar.HOUR, 1);// timeout, waiting for next hour
		}
		Log.info("waiting for next task until: " + cal.getTime());
		LockSupport.parkUntil(cal.getTimeInMillis());
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
		m_logger.error("TaskConsumer failed, " + doing.toString());

		doing.setStatus(STATUS_FAIL);
		doing.setEndDate(new Date());
		try {
			this.taskDao.updateDoingToFail(doing, TaskEntity.UPDATESET_FULL);
		} catch (DalException e) {
			e.printStackTrace();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#updateTodoStatus(com.dianping.cat.hadoop.dal.Task)
	 */
	@Override
	protected boolean updateTodoToDoing(Task todo) {
		todo.setStatus(STATUS_DOING);
		todo.setStartDate(new Date());
		try {
			return this.taskDao.updateTodoToDoing(todo, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			e.printStackTrace();
		}
		return true;
	}

}
