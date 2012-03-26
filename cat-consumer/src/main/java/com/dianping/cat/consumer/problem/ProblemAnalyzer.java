package com.dianping.cat.consumer.problem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.problem.handler.Handler;
import com.dianping.cat.consumer.problem.model.entity.AllDomains;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlParser;
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
	private List<Handler> m_handlers;

	private Map<String, ProblemReport> m_reports = new HashMap<String, ProblemReport>();

	private long m_extraTime;

	private Logger m_logger;

	private long m_startTime;

	private long m_duration;

	void closeMessageBuckets(Set<String> set) {
		Date timestamp = new Date(m_startTime);

		for (String domain : m_reports.keySet()) {
			Bucket<MessageTree> localBucket = null;
			Bucket<MessageTree> remoteBucket = null;

			try {
				localBucket = m_bucketManager.getMessageBucket(new Date(m_startTime), domain, "local");
				remoteBucket = m_bucketManager.getMessageBucket(new Date(m_startTime), domain, "remote");
			} catch (Exception e) {
				m_logger.error(String.format("Error when getting message bucket of %s!", timestamp), e);
			} finally {
				if (localBucket != null) {
					m_bucketManager.closeBucket(localBucket);
				}

				if (remoteBucket != null) {
					m_bucketManager.closeBucket(remoteBucket);
				}
			}
		}
	}

	@Override
	public void doCheckpoint() throws IOException {
		storeReports(m_reports.values());
		closeMessageBuckets(m_reports.keySet());
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
	protected List<ProblemReport> generate() {
		List<ProblemReport> reports = new ArrayList<ProblemReport>(m_reports.size());

		for (String domain : m_reports.keySet()) {
			ProblemReport report = getReport(domain);

			reports.add(report);
		}

		return reports;
	}

	public ProblemReport getReport(String domain) {
		ProblemReport report = m_reports.get(domain);

		if (report != null) {
			List<String> sortedDomains = getSortedDomains(m_reports.keySet());
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

	void loadReports() {
		Date timestamp = new Date(m_startTime);
		DefaultXmlParser parser = new DefaultXmlParser();
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(timestamp, "problem", "local");

			for (String id : bucket.getIdsByPrefix("")) {
				String xml = bucket.findById(id);
				ProblemReport report = parser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading problem reports of %s!", timestamp), e);
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
			String messageId = tree.getMessageId();

			try {
				Bucket<MessageTree> localBucket = m_bucketManager.getMessageBucket(new Date(m_startTime), domain, "local");
				Bucket<MessageTree> remoteBucket = m_bucketManager
				      .getMessageBucket(new Date(m_startTime), domain, "remote");

				localBucket.storeById(messageId, tree);
				remoteBucket.storeById(messageId, tree);
			} catch (IOException e) {
				m_logger.error("Error when storing message for problem analyzer!", e);
			}
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	@Override
	protected void store(List<ProblemReport> reports) {
		if (reports == null || reports.size() == 0) {
			return;
		}

		storeReports(reports);
		closeMessageBuckets(m_reports.keySet());
	}

	void storeReports(Collection<ProblemReport> reports) {
		Date timestamp = new Date(m_startTime);
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> localBucket = null;
		Bucket<String> remoteBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			localBucket = m_bucketManager.getReportBucket(timestamp, "problem", "local");
			remoteBucket = m_bucketManager.getReportBucket(timestamp, "problem", "remote");

			// delete old one, not append mode
			localBucket.deleteAndCreate();

			for (ProblemReport report : reports) {
				String xml = builder.buildXml(report);
				String domain = report.getDomain();

				localBucket.storeById(domain, xml);
				remoteBucket.storeById(domain, xml);
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing problem reports to %s!", timestamp), e);
		} finally {
			t.complete();

			if (localBucket != null) {
				m_bucketManager.closeBucket(localBucket);
			}

			if (remoteBucket != null) {
				m_bucketManager.closeBucket(remoteBucket);
			}
		}
	}
}
