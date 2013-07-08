package com.dianping.cat.consumer.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;

public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	public static final String ID = "transaction";

	@Inject(ID)
	private ReportManager<TransactionReport> m_reportManager;

	private Map<String, TransactionReport> m_reports = new HashMap<String, TransactionReport>();

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	// TODO remove it
	public Set<String> getDomains() {
		return m_reports.keySet();
	}

	@Override
	public TransactionReport getReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, false);
	}

	@Override
	protected void loadReports() {
		m_reports = m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		TransactionReport report = m_reports.get(domain);
		Message message = tree.getMessage();

		if (report == null) {
			report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
			m_reports.put(domain, report);
		}

		report.addIp(tree.getIpAddress());

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		}
	}

	private void processNameGraph(Transaction t, TransactionName name, int min, double d) {
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

	int processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		if (shouldDiscard(t)) {
			return 0;
		}

		String ip = tree.getIpAddress();
		TransactionType type = report.findOrCreateMachine(ip).findOrCreateType(t.getType());
		TransactionName name = type.findOrCreateName(t.getName());
		String messageId = tree.getMessageId();
		int count = 0;

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
		Integer allDuration = new Integer((int) duration);

		name.setMax(Math.max(name.getMax(), duration));
		name.setMin(Math.min(name.getMin(), duration));
		name.setSum(name.getSum() + duration);
		name.setSum2(name.getSum2() + duration * duration);
		name.findOrCreateAllDuration(allDuration).incCount();

		type.setMax(Math.max(type.getMax(), duration));
		type.setMin(Math.min(type.getMin(), duration));
		type.setSum(type.getSum() + duration);
		type.setSum2(type.getSum2() + duration * duration);
		type.findOrCreateAllDuration(allDuration).incCount();

		double d = t.getDurationInMicros() / 1000d;
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));

		processNameGraph(t, name, min, d);
		processTypeRange(t, type, min, d);

		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				count += processTransaction(report, tree, (Transaction) child);
			}
		}

		return count;
	}

	private void processTypeRange(Transaction t, TransactionType type, int min, double d) {
		Range2 range = type.findOrCreateRange2(min);

		if (!t.isSuccess()) {
			range.incFails();
		}
		range.incCount();
		range.setSum(range.getSum() + d);
	}
}
