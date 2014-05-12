package com.dianping.cat.consumer.transaction;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;

public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	private TransactionStatisticsComputer m_computer = new TransactionStatisticsComputer();

	@Inject
	private TransactionDelegate m_delegate;

	@Inject(ID)
	private ReportManager<TransactionReport> m_reportManager;

	@Inject
	private ServerConfigManager m_serverConfigManager;

	public static final String ID = "transaction";

	private Pair<Boolean, Long> checkForTruncatedMessage(MessageTree tree, Transaction t) {
		Pair<Boolean, Long> pair = new Pair<Boolean, Long>(true, t.getDurationInMicros());
		List<Message> children = t.getChildren();
		int size = children.size();

		if (tree.getMessage() == t && size > 0) { // root transaction with children
			Message last = children.get(size - 1);

			if (last instanceof Event) {
				String type = last.getType();
				String name = last.getName();

				if (type.equals("RemoteCall") && name.equals("Next")) {
					pair.setKey(false);
				} else if (type.equals("TruncatedTransaction") && name.equals("TotalDuration")) {
					try {
						long delta = Long.parseLong(last.getData().toString());

						pair.setValue(delta);
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
			}
		}

		return pair;
	}

	private double computeDuration(double duration) {
		if (duration < 20) {
			return duration;
		} else if (duration < 200) {
			return duration - duration % 5;
		} else if (duration < 2000) {
			return duration - duration % 50;
		} else {
			return duration - duration % 500;
		}
	}

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

	public Set<String> getDomains() {
		return m_reportManager.getDomains(getStartTime());
	}

	public TransactionReport getRawReport(String domain) {
		TransactionReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		return report;
	}

	@Override
	public TransactionReport getReport(String domain) {
		if (!Constants.ALL.equals(domain)) {
			try {
				return queryReport(domain);
			} catch (Exception e) {
				try {
					return queryReport(domain);
					// for concurrent modify exception
				} catch (ConcurrentModificationException ce) {
					Cat.logEvent("ConcurrentModificationException", domain, Event.SUCCESS, null);
					return new TransactionReport(domain);
				}
			}
		} else {
			Map<String, TransactionReport> reports = m_reportManager.getHourlyReports(getStartTime());

			return m_delegate.createAggregatedReport(reports);
		}
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		TransactionReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
		Message message = tree.getMessage();

		report.addIp(tree.getIpAddress());

		if (message instanceof Transaction) {
			Transaction root = (Transaction) message;

			processTransaction(report, tree, root);
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

	protected void processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		if (m_serverConfigManager.discardTransaction(t)) {
			return;
			// TODO remove me
		} else if ("ABTest".equals(t.getType())) {
			return;
		} else {
			String ip = tree.getIpAddress();
			TransactionType type = report.findOrCreateMachine(ip).findOrCreateType(t.getType());
			TransactionName name = type.findOrCreateName(t.getName());
			String messageId = tree.getMessageId();
			Pair<Boolean, Long> pair = checkForTruncatedMessage(tree, t);

			if (pair.getKey().booleanValue()) {
				processTypeAndName(t, type, name, messageId, pair.getValue().doubleValue() / 1000d);
			}

			List<Message> children = t.getChildren();

			for (Message child : children) {
				if (child instanceof Transaction) {
					processTransaction(report, tree, (Transaction) child);
				}
			}
		}
	}

	protected void processTypeAndName(Transaction t, TransactionType type, TransactionName name, String messageId,
	      double duration) {
		type.incTotalCount();
		name.incTotalCount();

		if (t.isSuccess()) {
			if (type.getSuccessMessageUrl() == null) {
				type.setSuccessMessageUrl(messageId);
			}

			if (name.getSuccessMessageUrl() == null) {
				name.setSuccessMessageUrl(messageId);
			}
		} else {
			type.incFailCount();
			name.incFailCount();

			if (type.getFailMessageUrl() == null) {
				type.setFailMessageUrl(messageId);
			}

			if (name.getFailMessageUrl() == null) {
				name.setFailMessageUrl(messageId);
			}
		}

		int allDuration = ((int) computeDuration(duration));

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

		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));

		processNameGraph(t, name, min, duration);
		processTypeRange(t, type, min, duration);
	}

	private void processTypeRange(Transaction t, TransactionType type, int min, double d) {
		Range2 range = type.findOrCreateRange2(min);

		if (!t.isSuccess()) {
			range.incFails();
		}

		range.incCount();
		range.setSum(range.getSum() + d);
	}

	private TransactionReport queryReport(String domain) {
		TransactionReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		report.accept(m_computer);

		return report;
	}
}
