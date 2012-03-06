package com.dianping.cat.consumer.transaction;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.configuration.model.entity.Property;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultJsonBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Files;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since Jan 5, 2012
 */
public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements Initializable,
      LogEnabled {
	private static final SimpleDateFormat FILE_SDF = new SimpleDateFormat("yyyyMMddHHmm");

	private static final long MINUTE = 60 * 1000;

	@Inject
	private MessageManager m_messageManager;

	@Inject
	private MessageStorage m_messageStorage;

	private Map<String, TransactionReport> m_reports = new HashMap<String, TransactionReport>();

	private long m_extraTime;

	private String m_reportPath;

	private Logger m_logger;

	private long m_startTime;

	private long m_duration;

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

	TransactionReport generate(String domain) {
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

	private String getTransactionFileName(TransactionReport report) {
		StringBuffer result = new StringBuffer();
		String start = FILE_SDF.format(report.getStartTime());
		String end = FILE_SDF.format(report.getEndTime());

		result.append(report.getDomain()).append("-").append(start).append("-").append(end);
		return result.toString();
	}

	@Override
	public void initialize() throws InitializationException {
		Config config = m_messageManager.getClientConfig();

		if (config != null) {
			Property property = config.findProperty("transaction-base-dir");

			if (property != null) {
				m_reportPath = property.getValue();
			}
		}
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
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
				m_messageStorage.store(tree);
			}
		}
	}

	int processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		TransactionType type = report.findOrCreateType(t.getType());
		TransactionName name = type.findOrCreateName(t.getName());
		String url = m_messageStorage.getPath(tree);
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
	}

	public void setMessageStorage(MessageStorage messageStorage) {
		m_messageStorage = messageStorage;
	}

	public void setReportPath(String reportPath) {
		m_reportPath = reportPath;
	}

	@Override
	protected void store(List<TransactionReport> reports) {
		if (reports == null || reports.size() == 0) {
			return;
		}

		for (TransactionReport report : reports) {
			String failureFileName = getTransactionFileName(report);
			String htmlPath = new StringBuilder().append(m_reportPath).append(failureFileName).append(".html").toString();
			File file = new File(htmlPath);

			file.getParentFile().mkdirs();

			try {
				Files.forIO().writeTo(file, new DefaultJsonBuilder().buildJson(report));
			} catch (IOException e) {
				m_logger.error(String.format("Error when writing to file(%s)!", file), e);
			}
		}
	}
}
