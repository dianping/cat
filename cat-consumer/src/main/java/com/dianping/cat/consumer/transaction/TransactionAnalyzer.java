package com.dianping.cat.consumer.transaction;

import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

@Named(type = MessageAnalyzer.class, value = TransactionAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {

	@Inject(ID)
	private ReportManager<TransactionReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private AtomicMessageConfigManager m_atomicMessageConfigManager;

	@Inject
	private TpValueStatisticConfigManager m_statisticManager;

	private TransactionStatisticsComputer m_computer = new TransactionStatisticsComputer();

	public static final String ID = "transaction";

	private int m_typeCountLimit = 100;

	private DurationMeta m_durationMeta = new DurationMeta();

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
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public Set<String> getDomains() {
		return m_reportManager.getDomains(getStartTime());
	}

	@Override
	public TransactionReport getReport(String domain) {
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
	}

	@Override
	public ReportManager<TransactionReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		if (tree.getTransactions().size() > 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		TransactionReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);

		report.addIp(tree.getIpAddress());

		List<Transaction> transactions = tree.getTransactions();

		for (Transaction t : transactions) {
			String data = String.valueOf(t.getData());

			if (data.length() > 0 && data.charAt(0) == CatConstants.BATCH_FLAG) {
				processBatchTransaction(tree, report, t, data);
			} else {
				processTransaction(report, tree, t);
			}
		}
	}

	private void processBatchTransaction(MessageTree tree, TransactionReport report, Transaction t, String data) {
		String[] tabs = data.substring(1).split(CatConstants.SPLIT);
		int total = Integer.parseInt(tabs[0]);
		int fail = Integer.parseInt(tabs[1]);
		long sum = Long.parseLong(tabs[2]);
		String type = t.getType();
		String name = t.getName();

		String ip = tree.getIpAddress();
		TransactionType transactionType = findOrCreateType(report.findOrCreateMachine(ip), type);
		TransactionName transactionName = findOrCreateName(transactionType, name, report.getDomain());
		DurationMeta durations = computeBatchDuration(t, tabs, transactionType, transactionName, report.getDomain());

		processTypeAndName(tree, t, transactionType, transactionName, total, fail, sum, durations);
	}

	private DurationMeta computeBatchDuration(Transaction t, String[] tabs, TransactionType transactionType,
							TransactionName transactionName, String domain) {
		if (tabs.length >= 4) {
			String duration = tabs[3];
			parseDurations(m_durationMeta, duration);

			Map<Integer, Integer> durations = m_durationMeta.getDurations();
			Map<Integer, AllDuration> allTypeDurations = transactionType.getAllDurations();
			Map<Integer, AllDuration> allNameDurations = transactionName.getAllDurations();
			long current = t.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Range2 typeRange = transactionType.findOrCreateRange2(min);
			Range nameRange = transactionName.findOrCreateRange(min);

			int maxValue = (int) m_durationMeta.getMax();
			int minValue = (int) m_durationMeta.getMin();

			transactionType.setMax(Math.max(maxValue, transactionType.getMax()));
			transactionType.setMin(Math.min(minValue, transactionType.getMin()));
			transactionName.setMax(Math.max(maxValue, transactionName.getMax()));
			transactionName.setMin(Math.min(minValue, transactionName.getMin()));
			typeRange.setMax(Math.max(maxValue, typeRange.getMax()));
			typeRange.setMin(Math.min(minValue, typeRange.getMin()));
			nameRange.setMax(Math.max(maxValue, nameRange.getMax()));
			nameRange.setMin(Math.min(minValue, nameRange.getMin()));

			mergeMap(allTypeDurations, durations);
			mergeMap(allNameDurations, durations);

			boolean statistic = m_statisticManager.shouldStatistic(t.getType(), domain);

			if (statistic) {
				mergeMap(typeRange.getAllDurations(), durations);
				mergeMap(nameRange.getAllDurations(), durations);
			}

			return m_durationMeta;
		}
		return null;
	}

	private void mergeMap(Map<Integer, AllDuration> allDurations, Map<Integer, Integer> other) {
		for (Map.Entry<Integer, Integer> entry : other.entrySet()) {
			Integer key = entry.getKey();
			Integer value = entry.getValue();
			AllDuration allDuration = allDurations.get(key);

			if (allDuration == null) {
				allDuration = new AllDuration(key);
				allDurations.put(key, allDuration);
			}

			allDuration.incCount(value);
		}
	}

	private void parseDurations(DurationMeta meta, String duration) {
		meta.clear();

		String[] tabs = duration.split("\\|");

		for (String tab : tabs) {
			String[] item = tab.split(",");

			if (item.length == 2) {
				meta.add(Integer.parseInt(item[0]), Integer.parseInt(item[1]));
			}
		}
	}

	private TransactionName findOrCreateName(TransactionType type, String name, String domain) {
		TransactionName transactionName = type.findName(name);

		if (transactionName == null) {
			int size = type.getNames().size();

			if (size > m_atomicMessageConfigManager.getMaxNameThreshold(domain)) {
				transactionName = type.findOrCreateName(CatConstants.OTHERS);
			} else {
				transactionName = type.findOrCreateName(name);
			}
		}

		return transactionName;
	}

	private TransactionType findOrCreateType(Machine machine, String type) {
		TransactionType transactionType = machine.findType(type);

		if (transactionType == null) {
			int size = machine.getTypes().size();

			if (size > m_typeCountLimit) {
				transactionType = machine.findOrCreateType(CatConstants.OTHERS);
			} else {
				transactionType = machine.findOrCreateType(type);
			}
		}

		return transactionType;
	}

	private void processTypeAndName(MessageTree tree, Transaction t, TransactionType type, TransactionName name, int total,
							int fail, long sum, DurationMeta durations) {
		String messageId = tree.getMessageId();

		if (type.getSuccessMessageUrl() == null) {
			type.setSuccessMessageUrl(messageId);
		}
		if (name.getSuccessMessageUrl() == null) {
			name.setSuccessMessageUrl(messageId);
		}

		if (type.getLongestMessageUrl() == null) {
			type.setLongestMessageUrl(messageId);
		}

		if (name.getLongestMessageUrl() == null) {
			name.setLongestMessageUrl(messageId);
		}

		type.incTotalCount(total);
		name.incTotalCount(total);

		type.incFailCount(fail);
		name.incFailCount(fail);

		type.setSum(type.getSum() + sum);
		name.setSum(name.getSum() + sum);

		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));

		processTypeRange(type, min, total, fail, sum);
		processNameGraph(name, min, total, fail, sum, durations);
	}

	private void processTypeRange(TransactionType type, int min, int total, int fail, long sum) {
		Range2 range = type.findOrCreateRange2(min);

		range.incCount(total);
		range.incFails(fail);
		range.setSum(range.getSum() + sum);
	}

	private void processNameGraph(TransactionName name, int min, int total, int fail, long sum, DurationMeta durations) {
		Range range = name.findOrCreateRange(min);

		range.incCount(total);
		range.incFails(fail);
		range.setSum(range.getSum() + sum);

		if (durations != null) {
			Map<Integer, Integer> ds = durations.getDurations();

			for (Map.Entry<Integer, Integer> entry : ds.entrySet()) {
				int formatDuration = formatDurationDistribute(entry.getKey());
				int count = entry.getValue();
				Duration duration = name.findOrCreateDuration(formatDuration);

				duration.incCount(count);
			}
		}
	}

	private int formatDurationDistribute(double d) {
		int dk = 1;

		if (d > 65536) {
			dk = 65536;
		} else {
			if (dk > 256) {
				dk = 256;
			}
			while (dk < d) {
				dk <<= 1;
			}
		}
		return dk;
	}

	private void processNameGraph(Transaction t, TransactionName name, int min, double d) {
		int dk = 1;

		if (d > 65536) {
			dk = 65536;
		} else {
			if (dk > 256) {
				dk = 256;
			}
			while (dk < d) {
				dk <<= 1;
			}
		}

		Duration duration = name.findOrCreateDuration(dk);
		Range range = name.findOrCreateRange(min);

		duration.incCount();
		range.incCount();

		if (!t.isSuccess()) {
			range.incFails();
		}

		range.setSum(range.getSum() + d);
	}

	protected void processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		String type = t.getType();
		String name = t.getName();

		if (m_serverFilterConfigManager.discardTransaction(type, name)) {
			return;
		} else {
			Pair<Boolean, Long> pair = checkForTruncatedMessage(tree, t);

			if (pair.getKey().booleanValue()) {
				String ip = tree.getIpAddress();
				TransactionType transactionType = report.findOrCreateMachine(ip).findOrCreateType(type);
				TransactionName transactionName = transactionType.findOrCreateName(name);
				String messageId = tree.getMessageId();

				processTypeAndName(t, transactionType, transactionName, messageId, pair.getValue().doubleValue() / 1000d);
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
		double sum = duration * duration;

		name.setMax(Math.max(name.getMax(), duration));
		name.setMin(Math.min(name.getMin(), duration));
		name.setSum(name.getSum() + duration);
		name.setSum2(name.getSum2() + sum);
		name.findOrCreateAllDuration(allDuration).incCount();

		type.setMax(Math.max(type.getMax(), duration));
		type.setMin(Math.min(type.getMin(), duration));
		type.setSum(type.getSum() + duration);
		type.setSum2(type.getSum2() + sum);
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
		long period = getStartTime();
		long timestamp = System.currentTimeMillis();
		long remainder = timestamp % ONE_HOUR;
		long current = timestamp - remainder;

		TransactionReport report = m_reportManager.getHourlyReport(period, domain, false);

		if (period == current) {
			report.accept(m_computer.setDuration(remainder / 1000));
		} else if (period < current) {
			report.accept(m_computer.setDuration(3600));
		}

		return report;
	}

	public static class DurationMeta {
		private Map<Integer, Integer> m_durations = new LinkedHashMap<Integer, Integer>();

		private double m_min = Integer.MAX_VALUE;

		private double m_max = -1;

		public void clear() {
			m_min = Integer.MAX_VALUE;
			m_max = -1;
			m_durations.clear();
		}

		public void add(Integer key, Integer value) {
			m_durations.put(key, value);

			if (m_min > key) {
				m_min = key;
			}
			if (m_max < key) {
				m_max = key;
			}
		}

		public Map<Integer, Integer> getDurations() {
			return m_durations;
		}

		public double getMax() {
			return m_max;
		}

		public double getMin() {
			return m_min;
		}
	}

}
