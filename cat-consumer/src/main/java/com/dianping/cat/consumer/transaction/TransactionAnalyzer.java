package com.dianping.cat.consumer.transaction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

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
import com.dianping.cat.storage.internal.AbstractFileBucket;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since Jan 5, 2012
 */
public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	private static final long MINUTE = 60 * 1000;

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	private Map<String, TransactionReport> m_reports = new HashMap<String, TransactionReport>();

	private Bucket<MessageTree> m_messageBucket;

	private long m_extraTime;

	private Logger m_logger;

	private long m_startTime;

	private long m_duration;

	@Override
	public void doCheckpoint() throws IOException {
		storeReports(m_reports.values());
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	protected List<TransactionReport> generate() {
		List<TransactionReport> reports = new ArrayList<TransactionReport>(m_reports.size());
		MeanSquareDeviationComputer computer = new MeanSquareDeviationComputer();

		for (String domain : m_reports.keySet()) {
			TransactionReport report = generate(domain);

			report.accept(computer);
			reports.add(report);
		}

		return reports;
	}

	public TransactionReport generate(String domain) {
		if (domain == null) {
			List<String> domains = getDomains();

			domain = domains.size() > 0 ? domains.get(0) : null;
		}

		TransactionReport report = m_reports.get(domain);

		return report;
	}

	public List<String> getDomains() {
		List<String> domains = new ArrayList<String>(m_reports.keySet());

		Collections.sort(domains, new Comparator<String>() {
			@Override
			public int compare(String d1, String d2) {
				if (d1.equals("Cat")) {
					return 1;
				}

				return d1.compareTo(d2);
			}
		});

		return domains;
	}

	public TransactionReport getReport(String domain) {
		return m_reports.get(domain);
	}

	public Map<String, TransactionReport> getReports() {
		return m_reports;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	void loadReports() {
		String path = m_pathBuilder.getReportPath(new Date(m_startTime));
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getStringBucket(path);

			if (bucket instanceof AbstractFileBucket) {
				DefaultXmlParser parser = new DefaultXmlParser();
				Set<String> ids = ((AbstractFileBucket<?>) bucket).getIds();

				for (String id : ids) {
					String xml = bucket.findById(id);
					TransactionReport report = parser.parse(xml);

					m_reports.put(report.getDomain(), report);
				}
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading transaction reports from %s!", path), e);
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
				String threadTag = "t:" + tree.getThreadId();
				String sessionTag = "s:" + tree.getSessionToken();
				String requestTag = "r:" + messageId;

				m_messageBucket.storeById(messageId, tree, threadTag, sessionTag, requestTag);
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

	public void setAnalyzerInfo(long startTime, long duration, String domain, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		String path = m_pathBuilder.getMessagePath(new Date(m_startTime));

		try {
			m_messageBucket = m_bucketManager.getMessageBucket(path);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Unable to create message bucket at %s.", path), e);
		}

		loadReports();
	}

	@Override
	protected void store(List<TransactionReport> reports) {
		if (reports == null || reports.size() == 0) {
			return;
		}

		m_bucketManager.closeBucket(m_messageBucket);
		storeReports(reports);
	}

	void storeReports(Collection<TransactionReport> reports) {
		String path = m_pathBuilder.getReportPath(new Date(m_startTime));
		Bucket<String> bucket = null;
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);

		try {
			bucket = m_bucketManager.getStringBucket(path);

			// delete old one, not append mode
			bucket.deleteAndCreate();
			
			for (TransactionReport report : reports) {
				bucket.storeById("transaction-" + report.getDomain(), builder.buildXml(report));
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when storing transaction reports to %s!", path), e);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}
}
