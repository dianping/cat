/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.consumer.transaction;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.analyzer.DurationComputer;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.transaction.model.entity.*;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Threads;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.*;

@Named(type = MessageAnalyzer.class, value = TransactionAnalyzer.ID, instantiationStrategy = Named.PER_LOOKUP)
public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {

	public static final String ID = "transaction";

	@Inject(ID)
	private ReportManager<TransactionReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_filterConfigManager;

	@Inject
	private TpValueStatisticConfigManager m_statisticManager;

	@Inject
	private AtomicMessageConfigManager m_atomicMessageConfigManager;

	private final TransactionStatisticsComputer m_computer = new TransactionStatisticsComputer();

	private int m_typeCountLimit = 100;

	private static final int m_statusCodeCountLimit = 100;

	private long m_nextClearTime;

	private final DurationMeta m_durationMeta = new DurationMeta();

	private boolean checkForTruncatedMessage(MessageTree tree, Transaction t) {
		List<Message> children = t.getChildren();
		int size = children.size();

		if (tree.getMessage() == t && size > 0) { // root transaction with children
			Message last = children.get(size - 1);

			if (last instanceof Event) {
				String type = last.getType();
				String name = last.getName();

				return !"RemoteCall".equals(type) || !"Next".equals(name);
			}
		}

		return true;
	}

	private void cleanUpReports() {
		String minute = TimeHelper.getMinuteStr();
		Transaction t = Cat.newTransaction("CleanUpTransactionReports", minute);

		try {
			Set<String> domains = m_reportManager.getDomains(m_startTime);

			m_computer.setMaxDurationMinute(m_serverConfigManager.getTpValueExpireMinute());

			for (String domain : domains) {
				Transaction tran = Cat.newTransaction("CleanUpTransaction", minute);

				tran.addData("domain", domain);

				TransactionReportCountFilter visitor = new TransactionReportCountFilter(m_serverConfigManager.getMaxTypeThreshold(),
										m_atomicMessageConfigManager.getMaxNameThreshold(domain), m_serverConfigManager.getTypeNameLengthLimit());

				try {
					TransactionReport transactionReport = m_reportManager.getHourlyReport(m_startTime, domain, false);

					m_computer.visitTransactionReport(transactionReport);
					visitor.visitTransactionReport(transactionReport);
					tran.setSuccessStatus();
				} catch (Exception e) {
					try {
						TransactionReport transactionReport = m_reportManager.getHourlyReport(m_startTime, domain, false);

						m_computer.visitTransactionReport(transactionReport);
						visitor.visitTransactionReport(transactionReport);
						tran.setSuccessStatus();
					} catch (Exception re) {
						Cat.logError(re);
						tran.setStatus(e);
					}
				} finally {
					tran.complete();
				}
			}
			t.setSuccessStatus();
		} catch (Exception e) {
			Cat.logError(e);
		} finally {
			t.complete();
		}
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

	private StatusCode findOrCreateStatusCode(TransactionName name, String codeName) {
		StatusCode code = name.findStatusCode(codeName);

		if (code == null) {
			int size = name.getStatusCodes().size();

			if (size > m_statusCodeCountLimit) {
				code = name.findOrCreateStatusCode(CatConstants.OTHERS);
			} else {
				code = name.findOrCreateStatusCode(codeName);
			}
		}
		return code;
	}

	private int formatDurationDistribute(double d) {
		int dk = 1;

		if (d > 65536) {
			dk = 65536;
		} else {
			while (dk < d) {
				dk <<= 1;
			}
		}
		return dk;
	}

	private String formatStatus(String status) {
		if (status.length() > 128) {
			return status.substring(0, 128);
		} else {
			return status;
		}
	}

	public Set<String> getDomains() {
		return m_reportManager.getDomains(getStartTime());
	}

	@Override
	public TransactionReport getReport(String domain) {
		TransactionReport report;
		try {
			report = queryReport(domain);
		} catch (Exception e) {
			try {
				report = queryReport(domain);
				// for concurrent modify exception
			} catch (ConcurrentModificationException ce) {
				Cat.logEvent("ConcurrentModificationException", domain, Event.SUCCESS, null);
				report = new TransactionReport(domain);
			}
		}
		// report.getIps().addAll(report.getMachines().keySet());

		return report;
	}

	@Override
	public ReportManager<TransactionReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	public void initialize(long startTime, long duration, long extraTime) {
		super.initialize(startTime, duration, extraTime);

		m_typeCountLimit = m_serverConfigManager.getMaxTypeThreshold();

		final long current = System.currentTimeMillis();

		if (startTime < current) {
			m_nextClearTime = TimeHelper.getCurrentMinute().getTime() + TimeHelper.ONE_MINUTE * 2;
		} else {
			m_nextClearTime = startTime + TimeHelper.ONE_MINUTE * 2;
		}
	}

	@Override
	public boolean isEligable(MessageTree tree) {
		List<Transaction> transactions = tree.getTransactions();

		return transactions != null && transactions.size() > 0;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
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

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		TransactionReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
		List<Transaction> transactions = tree.findOrCreateTransactions();

		for (Transaction t : transactions) {
			String data = String.valueOf(t.getData());

			if (data.length() > 0 && data.charAt(0) == CatConstants.BATCH_FLAG) {
				processBatchTransaction(tree, report, t, data);
			} else {
				processTransaction(report, tree, t);
			}
		}

		if (System.currentTimeMillis() > m_nextClearTime) {
			m_nextClearTime = m_nextClearTime + TimeHelper.ONE_MINUTE;

			Threads.forGroup("cat").start(new Runnable() {

				@Override
				public void run() {
					cleanUpReports();
				}
			});
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

	private void processNameGraph(Transaction t, TransactionName name, int min, double d, boolean statistic,
							int allDuration) {
		int dk = formatDurationDistribute(d);

		Duration duration = name.findOrCreateDuration(dk);
		Range range = name.findOrCreateRange(min);

		duration.incCount();
		range.incCount();

		if (!t.isSuccess()) {
			range.incFails();
		}

		range.setSum(range.getSum() + d);
		range.setMax(Math.max(range.getMax(), d));
		range.setMin(Math.min(range.getMin(), d));

		if (statistic) {
			range.findOrCreateAllDuration(allDuration).incCount();
		}
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

	private void processTransaction(TransactionReport report, MessageTree tree, Transaction t) {
		String type = t.getType();
		String name = t.getName();

		if (!m_filterConfigManager.discardTransaction(type, name)) {
			boolean valid = checkForTruncatedMessage(tree, t);

			if (valid) {
				String ip = tree.getIpAddress();
				TransactionType transactionType = findOrCreateType(report.findOrCreateMachine(ip), type);
				TransactionName transactionName = findOrCreateName(transactionType, name, report.getDomain());

				processTypeAndName(t, transactionType, transactionName, tree, t.getDurationInMillis());
			}
		}
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

	private void processTypeAndName(Transaction t, TransactionType type, TransactionName name, MessageTree tree,
							double duration) {
		String messageId = tree.getMessageId();

		type.incTotalCount();
		name.incTotalCount();

		type.setSuccessMessageUrl(messageId);
		name.setSuccessMessageUrl(messageId);

		if (!t.isSuccess()) {
			type.incFailCount();
			name.incFailCount();

			String statusCode = formatStatus(t.getStatus());

			findOrCreateStatusCode(name, statusCode).incCount();
		}

		int allDuration = DurationComputer.computeDuration((int) duration);
		double sum = duration * duration;

		if (type.getMax() <= duration) {
			type.setLongestMessageUrl(messageId);
		}
		if (name.getMax() <= duration) {
			name.setLongestMessageUrl(messageId);
		}
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
		boolean statistic = m_statisticManager.shouldStatistic(type.getId(), tree.getDomain());

		processNameGraph(t, name, min, duration, statistic, allDuration);
		processTypeRange(t, type, min, duration, statistic, allDuration);
	}

	private void processTypeRange(Transaction t, TransactionType type, int min, double d, boolean statistic,
							int allDuration) {
		Range2 range = type.findOrCreateRange2(min);

		if (!t.isSuccess()) {
			range.incFails();
		}

		range.incCount();
		range.setSum(range.getSum() + d);
		range.setMax(Math.max(range.getMax(), d));
		range.setMin(Math.min(range.getMin(), d));

		if (statistic) {
			range.findOrCreateAllDuration(allDuration).incCount();
		}
	}

	private void processTypeRange(TransactionType type, int min, int total, int fail, long sum) {
		Range2 range = type.findOrCreateRange2(min);

		range.incCount(total);
		range.incFails(fail);
		range.setSum(range.getSum() + sum);
	}

	private TransactionReport queryReport(String domain) {
		long period = getStartTime();
		long timestamp = System.currentTimeMillis();
		long remainder = timestamp % ONE_HOUR;
		long current = timestamp - remainder;

		TransactionReport report = m_reportManager.getHourlyReport(period, domain, false);

		m_computer.setMaxDurationMinute(m_serverConfigManager.getTpValueExpireMinute());

		if (period == current) {
			report.accept(m_computer.setDuration(remainder / 1000.0));
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
