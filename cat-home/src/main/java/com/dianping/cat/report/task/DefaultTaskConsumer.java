/**
 * 
 */
package com.dianping.cat.report.task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.xml.sax.SAXException;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
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

		int[] minuteCounts;

		double[] minuteNumbers;
	}

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
				return;
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

	private List<Graph> splitEventReportToGraphs(Date reportPeroid, String domainName, String reportName, EventReport eventReport) {
		Set<String> ips = eventReport.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1); // all and every machine
		Map<String, GraphLine> allDetailCache = new TreeMap<String, GraphLine>();
		Map<String, GraphLine> allSummaryCache = new TreeMap<String, GraphLine>();
		Date creationDate = new Date();
		for (String ip : ips) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(domainName);
			graph.setName(reportName);
			graph.setPeriod(reportPeroid);
			graph.setType(3);
			graph.setCreationDate(creationDate);
			com.dianping.cat.consumer.event.model.entity.Machine machine = eventReport.getMachines().get(ip);
			Map<String, EventType> types = machine.getTypes();
			StringBuilder detailBuilder = new StringBuilder();
			StringBuilder summaryBuilder = new StringBuilder();
			for (Entry<String, EventType> eventEntry : types.entrySet()) {
				EventType eventType = eventEntry.getValue();
				summaryBuilder.append(eventType.getId());
				summaryBuilder.append('\t');
				summaryBuilder.append(eventType.getTotalCount());
				summaryBuilder.append('\t');
				summaryBuilder.append(eventType.getFailCount());
				summaryBuilder.append('\n');

				String summaryKey = eventType.getId();
				GraphLine summaryLine = allSummaryCache.get(summaryKey);
				if (summaryLine == null) {
					summaryLine = new GraphLine();
					allSummaryCache.put(summaryKey, summaryLine);
				}

				summaryLine.totalCount += eventType.getTotalCount();
				summaryLine.failCount += eventType.getFailCount();
				Map<String, EventName> names = eventType.getNames();
				for (Entry<String, EventName> nameEntry : names.entrySet()) {
					EventName eventName = nameEntry.getValue();
					detailBuilder.append(eventType.getId());
					detailBuilder.append('\t');
					detailBuilder.append(eventName.getId());
					detailBuilder.append('\t');
					detailBuilder.append(eventName.getTotalCount());
					detailBuilder.append('\t');
					detailBuilder.append(eventName.getFailCount());
					detailBuilder.append('\n');

					String key = eventType.getId() + "\t" + eventName.getId();
					GraphLine detailLine = allDetailCache.get(key);
					if (detailLine == null) {
						detailLine = new GraphLine();
						allDetailCache.put(key, detailLine);
					}

					detailLine.totalCount += eventName.getTotalCount();
					detailLine.failCount += eventName.getFailCount();
				}
			}
			graph.setDetailContent(detailBuilder.toString());
			graph.setSummaryContent(summaryBuilder.toString());
			graphs.add(graph);
		}

		Graph allGraph = new Graph();
		allGraph.setIp("all");
		allGraph.setDomain(domainName);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeroid);
		allGraph.setType(3);
		allGraph.setCreationDate(creationDate);

		StringBuilder detailSb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allDetailCache.entrySet()) {
			detailSb.append(entry.getKey());
			detailSb.append('\t');
			GraphLine value = entry.getValue();
			detailSb.append(value.totalCount);
			detailSb.append('\t');
			detailSb.append(value.failCount);
			detailSb.append('\t');
			detailSb.append('\n');
		}
		allGraph.setDetailContent(detailSb.toString());

		StringBuilder summarySb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allSummaryCache.entrySet()) {
			summarySb.append(entry.getKey());
			summarySb.append('\t');
			GraphLine value = entry.getValue();
			summarySb.append(value.totalCount);
			summarySb.append('\t');
			summarySb.append(value.failCount);
			summarySb.append('\n');
		}
		allGraph.setSummaryContent(summarySb.toString());

		graphs.add(allGraph);

		return graphs;
	}

	private List<Graph> splitHeartbeatReportToGraphs(Date reportPeroid, String domainName, String reportName, HeartbeatReport heartbeatReport) {
		Set<String> ips = heartbeatReport.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size());

		for (String ip : ips) {
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(domainName);
			graph.setName(reportName);
			graph.setPeriod(reportPeroid);
			graph.setType(3);
			com.dianping.cat.consumer.heartbeat.model.entity.Machine machine = heartbeatReport.getMachines().get(ip);
			List<Period> periods = machine.getPeriods();

			Map<String, GraphLine> detailCache = new TreeMap<String, GraphLine>();

			for (Period period : periods) {
				int minute = period.getMinute();

				String key = "CatMessageSize";
				Number value = period.getCatMessageSize();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "CatMessageOverflow";
				value = period.getCatMessageOverflow();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "CatMessageProduced";
				value = period.getCatMessageProduced();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				List<Disk> disks = period.getDisks();
				for (Disk d : disks) {
					key = "Disk " + d.getPath();
					value = d.getFree();
					cacheHeartbeatColumn(detailCache, minute, value, key);
				}

				key = "MemoryFree";
				value = period.getMemoryFree();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "HeapUsage";
				value = period.getHeapUsage();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "NoneHeapUsage";
				value = period.getNoneHeapUsage();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "SystemLoadAverage";
				value = period.getSystemLoadAverage();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "OldGcCount";
				value = period.getOldGcCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "NewGcCount";
				value = period.getNewGcCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "PigeonStartedThread";
				value = period.getPigeonThreadCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "CatThreadCount";
				value = period.getCatThreadCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "TotalStartedThread";
				value = period.getTotalStartedCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "DaemonThread";
				value = period.getDaemonCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);

				key = "ActiveThread";
				value = period.getThreadCount();
				cacheHeartbeatColumn(detailCache, minute, value, key);
			}

			for (Entry<String, GraphLine> entry : detailCache.entrySet()) {
				GraphLine line = entry.getValue();
				double[] numbers = line.minuteNumbers;
				double minValue = numbers[0];
				double maxValue = minValue;
				double sum = minValue;
				double sum2 = sum * sum;

				for (int i = 1; i < numbers.length; i++) {
					double n = numbers[i];
					if (n > maxValue) {
						maxValue = n;
					}
					if (n < minValue) {
						minValue = n;
					}
					sum += n;
					sum2 += n * n;
				}

				line.min = minValue;
				line.max = maxValue;
				line.sum = sum;
				line.sum2 = sum2;
			}

			StringBuilder sb = new StringBuilder(64 * detailCache.size());
			for (Entry<String, GraphLine> entry : detailCache.entrySet()) {
				GraphLine value = entry.getValue();
				sb.append(entry.getKey());
				sb.append('\t');
				sb.append(value.min);
				sb.append('\t');
				sb.append(value.max);
				sb.append('\t');
				sb.append(value.sum);
				sb.append('\t');
				sb.append(value.sum2);
				sb.append('\t');
				sb.append(TaskHelper.join(value.minuteNumbers, ','));
				sb.append('\n');
			}

			graph.setDetailContent(sb.toString());
			graph.setCreationDate(new Date());

			graphs.add(graph);

		}
		return graphs;
	}

	private void cacheHeartbeatColumn(Map<String, GraphLine> detailCache, int minute, Number value, String key) {
		GraphLine detailLine = detailCache.get(key);
		if (detailLine == null) {
			detailLine = new GraphLine();
			detailLine.minuteNumbers = new double[60];
			detailCache.put(key, detailLine);
		}
		detailLine.minuteNumbers[minute] = value.doubleValue();
	}

	private List<Graph> splitProblemReportToGraphs(Date reportPeroid, String domainName, String reportName, ProblemReport problemReport) {
		Set<String> ips = problemReport.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1); // all and every machine
		Map<String, GraphLine> allDetailCache = new TreeMap<String, GraphLine>();
		Map<String, GraphLine> allSummaryCache = new TreeMap<String, GraphLine>();

		for (String ip : ips) {
			Map<String, GraphLine> detailCache = new TreeMap<String, GraphLine>();
			Map<String, GraphLine> summaryCache = new TreeMap<String, GraphLine>();
			Graph graph = new Graph();
			graph.setIp(ip);
			graph.setDomain(domainName);
			graph.setName(reportName);
			graph.setPeriod(reportPeroid);
			graph.setType(3);
			com.dianping.cat.consumer.problem.model.entity.Machine machine = problemReport.getMachines().get(ip);
			Map<String, JavaThread> types = machine.getThreads();

			for (Entry<String, JavaThread> transactionEntry : types.entrySet()) {
				JavaThread thread = transactionEntry.getValue();
				for (Entry<Integer, Segment> segmentEntry : thread.getSegments().entrySet()) {
					Segment segment = segmentEntry.getValue();
					int minute = segment.getId();
					for (com.dianping.cat.consumer.problem.model.entity.Entry entry : segment.getEntries()) {
						String summaryKey = entry.getType();
						GraphLine summaryLine = summaryCache.get(summaryKey);
						if (summaryLine == null) {
							summaryLine = new GraphLine();
							summaryLine.minuteCounts = new int[60];
							summaryCache.put(summaryKey, summaryLine);
						}
						summaryLine.totalCount++;
						summaryLine.minuteCounts[minute]++;

						GraphLine allSummaryLine = allSummaryCache.get(summaryKey);
						if (allSummaryLine == null) {
							allSummaryLine = new GraphLine();
							allSummaryLine.minuteCounts = new int[60];
							allSummaryCache.put(summaryKey, allSummaryLine);
						}
						allSummaryLine.totalCount++;
						allSummaryLine.minuteCounts[minute]++;

						String detailKey = entry.getType() + "\t" + entry.getStatus();
						GraphLine detailLine = detailCache.get(detailKey);
						if (detailLine == null) {
							detailLine = new GraphLine();
							detailLine.minuteCounts = new int[60];
							detailCache.put(detailKey, detailLine);
						}
						detailLine.totalCount++;
						detailLine.minuteCounts[minute]++;

						GraphLine allDetailLine = allDetailCache.get(detailKey);
						if (allDetailLine == null) {
							allDetailLine = new GraphLine();
							allDetailLine.minuteCounts = new int[60];
							allDetailCache.put(detailKey, allDetailLine);
						}
						allDetailLine.totalCount++;
						allDetailLine.minuteCounts[minute]++;
					}
				}

			}

			StringBuilder summaryBuilder = new StringBuilder();
			for (Entry<String, GraphLine> summaryEntry : summaryCache.entrySet()) {
				GraphLine summaryLine = summaryEntry.getValue();
				summaryBuilder.append(summaryEntry.getKey());
				summaryBuilder.append("\t");
				summaryBuilder.append(summaryLine.totalCount);
				summaryBuilder.append("\t");
				summaryBuilder.append(TaskHelper.join(summaryLine.minuteCounts, ','));
				summaryBuilder.append("\n");
			}
			graph.setSummaryContent(summaryBuilder.toString());

			StringBuilder detailBuilder = new StringBuilder();
			for (Entry<String, GraphLine> detailEntry : detailCache.entrySet()) {
				GraphLine detailLine = detailEntry.getValue();
				detailBuilder.append(detailEntry.getKey());
				detailBuilder.append("\t");
				detailBuilder.append(detailLine.totalCount);
				detailBuilder.append("\t");
				detailBuilder.append(TaskHelper.join(detailLine.minuteCounts, ','));
				detailBuilder.append("\n");
			}
			graph.setDetailContent(detailBuilder.toString());

			graph.setCreationDate(new Date());
			graphs.add(graph);
		}

		Graph allGraph = new Graph();
		allGraph.setIp("all");
		allGraph.setDomain(domainName);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeroid);
		allGraph.setType(3);

		StringBuilder summaryBuilder = new StringBuilder();
		for (Entry<String, GraphLine> summaryEntry : allSummaryCache.entrySet()) {
			GraphLine summaryLine = summaryEntry.getValue();
			summaryBuilder.append(summaryEntry.getKey());
			summaryBuilder.append("\t");
			summaryBuilder.append(summaryLine.totalCount);
			summaryBuilder.append("\t");
			summaryBuilder.append(TaskHelper.join(summaryLine.minuteCounts, ','));
			summaryBuilder.append("\n");
		}
		allGraph.setSummaryContent(summaryBuilder.toString());

		StringBuilder detailBuilder = new StringBuilder();
		for (Entry<String, GraphLine> detailEntry : allDetailCache.entrySet()) {
			GraphLine detailLine = detailEntry.getValue();
			detailBuilder.append(detailEntry.getKey());
			detailBuilder.append("\t");
			detailBuilder.append(detailLine.totalCount);
			detailBuilder.append("\t");
			detailBuilder.append(TaskHelper.join(detailLine.minuteCounts, ','));
			detailBuilder.append("\n");
		}
		allGraph.setDetailContent(detailBuilder.toString());

		allGraph.setCreationDate(new Date());

		graphs.add(allGraph);

		return graphs;
	}

	private List<Graph> splitTransactionReportToGraphs(Date reportPeroid, String domainName, String reportName, TransactionReport transactionReport) {
		Set<String> ips = transactionReport.getIps();
		List<Graph> graphs = new ArrayList<Graph>(ips.size() + 1); // all and every machine
		Map<String, GraphLine> allDetailCache = new TreeMap<String, GraphLine>();
		Map<String, GraphLine> allSummaryCache = new TreeMap<String, GraphLine>();
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
			StringBuilder detailBuilder = new StringBuilder();
			StringBuilder summaryBuilder = new StringBuilder();
			for (Entry<String, TransactionType> transactionEntry : types.entrySet()) {
				TransactionType transactionType = transactionEntry.getValue();
				summaryBuilder.append(transactionType.getId());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getTotalCount());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getFailCount());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getMin());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getMax());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getSum());
				summaryBuilder.append('\t');
				summaryBuilder.append(transactionType.getSum2());
				summaryBuilder.append('\n');

				String summaryKey = transactionType.getId();
				GraphLine summaryLine = allSummaryCache.get(summaryKey);
				if (summaryLine == null) {
					summaryLine = new GraphLine();
					allSummaryCache.put(summaryKey, summaryLine);
				}

				summaryLine.totalCount += transactionType.getTotalCount();
				summaryLine.failCount += transactionType.getFailCount();
				summaryLine.min += transactionType.getMin();
				summaryLine.max += transactionType.getMax();
				summaryLine.sum += transactionType.getSum();
				summaryLine.sum2 += transactionType.getSum2();
				Map<String, TransactionName> names = transactionType.getNames();
				for (Entry<String, TransactionName> nameEntry : names.entrySet()) {
					TransactionName transactionName = nameEntry.getValue();
					detailBuilder.append(transactionType.getId());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getId());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getTotalCount());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getFailCount());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getMin());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getMax());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getSum());
					detailBuilder.append('\t');
					detailBuilder.append(transactionName.getSum2());
					detailBuilder.append('\n');

					String key = transactionType.getId() + "\t" + transactionName.getId();
					GraphLine detailLine = allDetailCache.get(key);
					if (detailLine == null) {
						detailLine = new GraphLine();
						allDetailCache.put(key, detailLine);
					}

					detailLine.totalCount += transactionName.getTotalCount();
					detailLine.failCount += transactionName.getFailCount();
					detailLine.min += transactionName.getMin();
					detailLine.max += transactionName.getMax();
					detailLine.sum += transactionName.getSum();
					detailLine.sum2 += transactionName.getSum2();
				}
			}
			graph.setDetailContent(detailBuilder.toString());
			graph.setSummaryContent(summaryBuilder.toString());
			graphs.add(graph);
		}

		Graph allGraph = new Graph();
		allGraph.setIp("all");
		allGraph.setDomain(domainName);
		allGraph.setName(reportName);
		allGraph.setPeriod(reportPeroid);
		allGraph.setType(3);
		allGraph.setCreationDate(creationDate);

		StringBuilder detailSb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allDetailCache.entrySet()) {
			detailSb.append(entry.getKey());
			detailSb.append('\t');
			GraphLine value = entry.getValue();
			detailSb.append(value.totalCount);
			detailSb.append('\t');
			detailSb.append(value.failCount);
			detailSb.append('\t');
			detailSb.append(value.min);
			detailSb.append('\t');
			detailSb.append(value.max);
			detailSb.append('\t');
			detailSb.append(value.sum);
			detailSb.append('\t');
			detailSb.append(value.sum2);
			detailSb.append('\n');
		}
		allGraph.setDetailContent(detailSb.toString());

		StringBuilder summarySb = new StringBuilder();
		for (Entry<String, GraphLine> entry : allSummaryCache.entrySet()) {
			summarySb.append(entry.getKey());
			summarySb.append('\t');
			GraphLine value = entry.getValue();
			summarySb.append(value.totalCount);
			summarySb.append('\t');
			summarySb.append(value.failCount);
			summarySb.append('\t');
			summarySb.append(value.min);
			summarySb.append('\t');
			summarySb.append(value.max);
			summarySb.append('\t');
			summarySb.append(value.sum);
			summarySb.append('\t');
			summarySb.append(value.sum2);
			summarySb.append('\n');
		}
		allGraph.setSummaryContent(summarySb.toString());

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
