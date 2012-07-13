package com.dianping.cat.consumer.problem;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.handler.Handler;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class ProblemAnalyzer extends AbstractMessageAnalyzer<ProblemReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	@Inject
	private List<Handler> m_handlers;

	private Map<String, ProblemReport> m_reports = new HashMap<String, ProblemReport>();

	private long m_extraTime;

	private Logger m_logger;

	private long m_startTime;

	private long m_duration;

	private void closeMessageBuckets() {
		Date timestamp = new Date(m_startTime);

		for (String domain : m_reports.keySet()) {
			Bucket<MessageTree> logviewBucket = null;

			try {
				logviewBucket = m_bucketManager.getLogviewBucket(m_startTime, domain);
			} catch (Exception e) {
				m_logger.error(String.format("Error when getting logview bucket of %s!", timestamp), e);
			} finally {
				if (logviewBucket != null) {
					m_bucketManager.closeBucket(logviewBucket);
				}
			}
		}
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
		closeMessageBuckets();
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
		}
		report.getDomainNames().addAll(m_reports.keySet());

		return report;
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
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		ProblemReport report = m_reports.get(domain);

		if (report == null) {
			report = new ProblemReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		report.addIp(tree.getIpAddress());
		//Machine machine = findOrCreateMachine(report, tree);
		Machine machine = report.findOrCreateMachine(tree.getIpAddress());
		int count = 0;

		for (Handler handler : m_handlers) {
			count += handler.handle(machine, tree);
		}

		if (count > 0) {
			storeMessage(tree);
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	private void storeMessage(MessageTree tree) {
		String messageId = tree.getMessageId();
		String domain = tree.getDomain();

		try {
			Bucket<MessageTree> logviewBucket = m_bucketManager.getLogviewBucket(m_startTime, domain);

			logviewBucket.storeById(messageId, tree);
		} catch (Exception e) {
			m_logger.error("Error when storing message for problem analyzer!", e);
		}
	}

	private void storeReports(boolean atEnd) {

		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "problem");

			for (ProblemReport report : m_reports.values()) {
				Set<String> domainNames = report.getDomainNames();
				domainNames.clear();
				domainNames.addAll(getDomains());

				String xml = builder.buildXml(report);
				String domain = report.getDomain();

				reportBucket.storeById(domain, xml);
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

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
	               task.setStatus(1); // status todo
	               m_taskDao.insert(task);
	               m_logger.info("insert event task:" + task.toString());
               } catch (Throwable e) {
         			Cat.getProducer().logError(e);
               }
				}
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing problem reports to %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}
}
