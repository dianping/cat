package com.dianping.cat.consumer.problem;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.Task;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.handler.Handler;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class ProblemAnalyzer extends AbstractMessageAnalyzer<ProblemReport> implements LogEnabled, Initializable {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	@Inject
	private List<Handler> m_handlers;

	private Map<String, ProblemReport> m_reports = new HashMap<String, ProblemReport>();

	private ProblemReport buildTotalProblemReport() {
		ProblemReport report = new ProblemReport(ALL);
		ProblemReportVisitor visitor = new ProblemReportVisitor(report);

		try {
			for (ProblemReport temp : m_reports.values()) {
				report.getIps().add(temp.getDomain());
				report.getDomainNames().add(temp.getDomain());
				visitor.visitProblemReport(temp);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return report;
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains() {
		return m_reports.keySet();
	}

	public ProblemReport getReport(String domain) {
		ProblemReport report = m_reports.get(domain);

		if (report == null) {
			report = new ProblemReport(domain);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}
		report.getDomainNames().addAll(m_reports.keySet());

		return report;
	}

	@Override
	public void initialize() throws InitializationException {
		// to work around a performance issue within plexus
		m_handlers = new ArrayList<Handler>(m_handlers);
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	private void loadReports() {
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(m_startTime, "problem");

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				ProblemReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading problem reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		ProblemReport report = m_reports.get(domain);

		if (report == null) {
			report = new ProblemReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		report.addIp(tree.getIpAddress());
		Machine machine = report.findOrCreateMachine(tree.getIpAddress());

		for (Handler handler : m_handlers) {
			handler.handle(machine, tree);
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "problem");

			for (ProblemReport report : m_reports.values()) {
				try {
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(getDomains());

					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.getProducer().logError(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				ProblemReport all = buildTotalProblemReport();

				m_reports.put(ALL, all);

				for (ProblemReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName("problem");
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);

						Task task = m_taskDao.createLocal();
						task.setCreationDate(new Date());
						task.setProducer(ip);
						task.setReportDomain(domain);
						task.setReportName("problem");
						task.setReportPeriod(period);
						task.setStatus(1);
						m_taskDao.insert(task);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.getProducer().logError(e);
					}
				}
			}
		} catch (Exception e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			m_logger.error(String.format("Error when storing problem reports to %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	static class ProblemReportVisitor extends BaseVisitor {

		private ProblemReport m_report;

		private String m_currentDomain;

		private String m_currentType;

		private String m_currentState;

		public ProblemReportVisitor(ProblemReport report) {
			m_report = report;
		}

		protected Entry findOrCreatEntry(Machine machine, String type, String status) {
			List<Entry> entries = machine.getEntries();

			for (Entry entry : entries) {
				if (entry.getType().equals(type) && entry.getStatus().equals(status)) {
					return entry;
				}
			}
			Entry entry = new Entry();

			entry.setStatus(status);
			entry.setType(type);
			entries.add(entry);
			return entry;
		}

		@Override
		public void visitDuration(Duration duration) {
			int value = duration.getValue();
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);
			Entry entry = findOrCreatEntry(machine, m_currentType, m_currentState);
			Duration temp = entry.findOrCreateDuration(value);

			temp.setCount(temp.getCount() + duration.getCount());
		}

		@Override
		public void visitEntry(Entry entry) {
			m_currentType = entry.getType();
			m_currentState = entry.getStatus();
			super.visitEntry(entry);
		}

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			m_currentDomain = problemReport.getDomain();
			super.visitProblemReport(problemReport);
		}

	}
}
