package com.dianping.cat.consumer.transaction;

import java.io.IOException;
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
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	private Map<String, TransactionReport> m_reports = new HashMap<String, TransactionReport>();

	private long m_extraTime;

	private long m_startTime;

	private long m_duration;

	private Logger m_logger;

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

	@Override
	public TransactionReport getReport(String domain) {
		TransactionReport report = m_reports.get(domain);

		if (report == null) {
			report = new TransactionReport(domain);
		}

		report.getDomainNames().clear();
		report.getDomainNames().addAll(m_reports.keySet());

		report.accept(new StatisticsComputer());
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
			bucket = m_bucketManager.getReportBucket(m_startTime, "transaction");

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				TransactionReport report = parser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading transacion reports of %s!", new Date(m_startTime)), e);
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
				storeMessage(tree);
			}
		}
	}

	int processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		TransactionType type = report.findOrCreateType(t.getType());
		TransactionName name = type.findOrCreateName(t.getName());
		String messageId = tree.getMessageId();
		int count = 0;

		synchronized (type) {
			type.incTotalCount();
			name.incTotalCount();

			if (t.isSuccess()) {
				if (type.getSuccessMessageUrl() == null) {
					type.setSuccessMessageUrl(messageId);
					count++;
				}

				if (name.getSuccessMessageUrl() == null) {
					name.setSuccessMessageUrl(messageId);
					count++;
				}
			} else {
				type.incFailCount();
				name.incFailCount();

				if (type.getFailMessageUrl() == null) {
					type.setFailMessageUrl(messageId);
					count++;
				}

				if (name.getFailMessageUrl() == null) {
					name.setFailMessageUrl(messageId);
					count++;
				}
			}

			// update statistics
			double duration = t.getDurationInMicros() / 1000d;

			name.setMax(Math.max(name.getMax(), duration));
			name.setMin(Math.min(name.getMin(), duration));
			name.setSum(name.getSum() + duration);
			name.setSum2(name.getSum2() + duration * duration);

			type.setMax(Math.max(type.getMax(), duration));
			type.setMin(Math.min(type.getMin(), duration));
			type.setSum(type.getSum() + duration);
			type.setSum2(type.getSum2() + duration * duration);
		}

		processTransactionGrpah(name, t);

		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				count += processTransaction(report, tree, (Transaction) child);
			}
		}

		return count;
	}

	private void processTransactionGrpah(TransactionName name, Transaction t) {
		double d = t.getDurationInMicros() / 1000d;
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

		synchronized (name) {
			duration.incCount();
			range.incCount();

			if (!t.isSuccess()) {
				range.incFails();
			}

			range.setSum(range.getSum() + d);
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
		} catch (IOException e) {
			m_logger.error("Error when storing logview for transaction analyzer!", e);
		}
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "transaction");

			for (TransactionReport report : m_reports.values()) {
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

				for (TransactionReport report : m_reports.values()) {
					Report r = m_reportDao.createLocal();
					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					r.setName("transaction");
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
			m_logger.error(String.format("Error when storing transaction reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}
}
