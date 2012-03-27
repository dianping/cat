package com.dianping.cat.consumer.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	private static final long MINUTE = 60 * 1000;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	private Map<String, TransactionReport> m_reports = new HashMap<String, TransactionReport>();

	private long m_extraTime;

	private Logger m_logger;

	private long m_startTime;

	private long m_duration;

	private boolean m_local;

	void closeMessageBuckets() {
		Date timestamp = new Date(m_startTime);

		for (String domain : m_reports.keySet()) {
			Bucket<MessageTree> localBucket = null;
			Bucket<MessageTree> remoteBucket = null;

			try {
				localBucket = m_bucketManager.getMessageBucket(timestamp, domain, "local");

				if (!m_local) {
					remoteBucket = m_bucketManager.getMessageBucket(timestamp, domain, "remote");
				}
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
		closeMessageBuckets();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	protected List<TransactionReport> generate() {
		List<TransactionReport> reports = new ArrayList<TransactionReport>(m_reports.size());
		StatisticsComputer computer = new StatisticsComputer();

		for (String domain : m_reports.keySet()) {
			TransactionReport report = getReport(domain);

			report.accept(computer);
			reports.add(report);
		}

		return reports;
	}

	@Override
	public TransactionReport getReport(String domain) {
		TransactionReport report = m_reports.get(domain);

		if (report != null) {
			List<String> sortedDomains = getSortedDomains(m_reports.keySet());

			for (String e : sortedDomains) {
				report.addDomain(e);
			}
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
			bucket = m_bucketManager.getReportBucket(timestamp, "transaction", "local");

			for (String id : bucket.getIdsByPrefix("")) {
				String xml = bucket.findById(id);
				TransactionReport report = parser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading transacion reports of %s!", timestamp), e);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		TransactionReport report = m_reports.get(domain);

		if (report == null) {
			report = new TransactionReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			int count = processTransaction(report, tree, (Transaction) message);

			// the message is required by some transactions
			if (count > 0) {
				String messageId = tree.getMessageId();
				Transaction t = Cat.getProducer().newTransaction("MessageProcess", getClass().getSimpleName());
				t.setStatus(Message.SUCCESS);

				try {
					Bucket<MessageTree> localBucket = m_bucketManager.getMessageBucket(new Date(m_startTime), domain, "local");
					Bucket<MessageTree> remoteBucket = m_bucketManager.getMessageBucket(new Date(m_startTime), domain, "remote");

					localBucket.storeById(messageId, tree);
					remoteBucket.storeById(messageId, tree);
				} catch (IOException e) {
					// we don't let transaction status to fail to avoid message storm
					m_logger.error("Error when storing message for transaction analyzer!", e);
				} finally {
					t.complete();
				}
			}
		}
	}

	int processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		TransactionType type = report.findOrCreateType(t.getType());
		TransactionName name = type.findOrCreateName(t.getName());
		String url = m_pathBuilder.getLogViewPath(tree.getMessageId());
		int count = 0;

		type.incTotalCount();
		name.incTotalCount();

		if (t.isSuccess()) {
			if (type.getSuccessMessageUrl() == null) {
				type.setSuccessMessageUrl(url);
				count++;
			}

			if (name.getSuccessMessageUrl() == null) {
				name.setSuccessMessageUrl(url);
				count++;
			}
		} else {
			type.incFailCount();
			name.incFailCount();

			if (type.getFailMessageUrl() == null) {
				type.setFailMessageUrl(url);
				count++;
			}

			if (name.getFailMessageUrl() == null) {
				name.setFailMessageUrl(url);
				count++;
			}
		}

		// update statistics
		long duration = t.getDuration();

		name.setMax(Math.max(name.getMax(), duration));
		name.setMin(Math.min(name.getMin(), duration));
		name.setSum(name.getSum() + duration);
		name.setSum2(name.getSum2() + duration * duration);

		type.setMax(Math.max(type.getMax(), duration));
		type.setMin(Math.min(type.getMin(), duration));
		type.setSum(type.getSum() + duration);
		type.setSum2(type.getSum2() + duration * duration);

		processTransactionGrpah(name, t);

		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				count += processTransaction(report, tree, (Transaction) child);
			}
		}

		return count;
	}

	void processTransactionGrpah(TransactionName name, Transaction t) {
		long d = t.getDuration();
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(t.getTimestamp());
		int min = cal.get(Calendar.MINUTE);
		int dk = 1;
		int tk = min - min % 5;

		while (dk < d) {
			dk <<= 1;
		}

		Duration duration = name.findOrCreateDuration(dk);
		Range range = name.findOrCreateRange(tk);

		duration.incCount();
		range.incCount();

		if (!t.isSuccess()) {
			range.incFails();
		}

		range.setSum(range.getSum() + d);
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	public void setLocal(boolean local) {
		m_local = local;
	}

	@Override
	protected void store(List<TransactionReport> reports) {
		if (reports == null || reports.size() == 0) {
			return;
		}

		storeReports(reports);
		closeMessageBuckets();
	}

	void storeMessage(MessageTree tree) {
		String messageId = tree.getMessageId();
		String domain = tree.getDomain();

		try {
			Bucket<MessageTree> localBucket = m_bucketManager.getMessageBucket(new Date(m_startTime), domain, "local");

			localBucket.storeById(messageId, tree);

			if (!m_local) {
				Bucket<MessageTree> remoteBucket = m_bucketManager
				      .getMessageBucket(new Date(m_startTime), domain, "remote");

				remoteBucket.storeById(messageId, tree);
			}
		} catch (IOException e) {
			m_logger.error("Error when storing message for transaction analyzer!", e);
		}
	}

	void storeReports(Collection<TransactionReport> reports) {
		Date timestamp = new Date(m_startTime);
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());
		Bucket<String> localBucket = null;
		Bucket<String> remoteBucket = null;

		try {
			localBucket = m_bucketManager.getReportBucket(timestamp, "transaction", "local");

			if (!m_local) {
				remoteBucket = m_bucketManager.getReportBucket(timestamp, "transaction", "remote");
			}

			// delete old one, not append mode
			localBucket.deleteAndCreate();

			for (TransactionReport report : reports) {
				String xml = builder.buildXml(report);
				String domain = report.getDomain();

				localBucket.storeById(domain, xml);

				if (!m_local) {
					remoteBucket.storeById(domain, xml);
				}
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing transaction reports of %s!", timestamp), e);
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
