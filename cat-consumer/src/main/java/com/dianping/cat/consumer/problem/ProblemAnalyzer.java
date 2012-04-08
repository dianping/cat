package com.dianping.cat.consumer.problem;

import java.util.Calendar;
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
import com.dianping.cat.consumer.problem.model.entity.AllDomains;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class ProblemAnalyzer extends AbstractMessageAnalyzer<ProblemReport> implements LogEnabled {
	private static final long MINUTE = 60 * 1000;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

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

	private Segment findOrCreateSegment(ProblemReport report, MessageTree tree) {
		Machine machine = report.findOrCreateMachine(tree.getIpAddress());
		JavaThread thread = machine.findOrCreateThread(tree.getThreadId());
		thread.setGroupName(tree.getThreadGroupName()).setName(tree.getThreadName());

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(tree.getMessage().getTimestamp());

		int minute = cal.get(Calendar.MINUTE);
		Segment segment = thread.findOrCreateSegment(minute);
		return segment;
	}

	@Override
	public Set<String> getDomains() {
		return m_reports.keySet();
	}

	public ProblemReport getReport(String domain) {
		ProblemReport report = m_reports.get(domain);

		if (report != null) {
			List<String> sortedDomains = sortDomains(m_reports.keySet());
			AllDomains allDomains = new AllDomains();

			for (String e : sortedDomains) {
				allDomains.addDomain(e);
			}

			report.setAllDomains(allDomains);
		}

		return report;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	private void loadReports() {
		DefaultXmlParser parser = new DefaultXmlParser();
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(m_startTime, "problem");

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				ProblemReport report = parser.parse(xml);

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

		Segment segment = findOrCreateSegment(report, tree);
		int count = 0;

		for (Handler handler : m_handlers) {
			count += handler.handle(segment, tree);
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
				String xml = builder.buildXml(report);
				String domain = report.getDomain();

				reportBucket.storeById(domain, xml);
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (ProblemReport report : m_reports.values()) {
					Report r = m_reportDao.createLocal();
					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					r.setName("event");
					r.setDomain(domain);
					r.setPeriod(period);
					r.setIp(ip);
					r.setType(1);
					r.setContent(xml);

					m_reportDao.insert(r);
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
