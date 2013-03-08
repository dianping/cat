package com.dianping.cat.consumer.transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.Task;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.BaseVisitor;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class TransactionAnalyzer extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private Map<String, TransactionReport> m_reports = new HashMap<String, TransactionReport>();

	private void clearAllDuration(TransactionReport report) {
		Collection<Machine> machines = report.getMachines().values();

		for (Machine machine : machines) {
			for (TransactionType type : machine.getTypes().values()) {
				type.getAllDurations().clear();

				for (TransactionName name : type.getNames().values()) {
					name.getAllDurations().clear();
				}
			}
		}
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private double get95Line(Map<Integer, AllDuration> durations) {
		int totalCount = 0;

		for (AllDuration duration : durations.values()) {
			totalCount += duration.getCount();
		}

		int index = totalCount * 5 / 100;
		Map<Integer, AllDuration> result = getSortDuration(durations);

		for (Entry<Integer, AllDuration> entry : result.entrySet()) {
			index = index - entry.getValue().getCount();
			if (index <= 0) {
				return entry.getKey();
			}
		}
		return 0;
	}

	@Override
	public Set<String> getDomains() {
		Set<String> keySet = m_reports.keySet();
		return keySet;
	}

	@Override
	public TransactionReport getReport(String domain) {
		TransactionReport report = m_reports.get(domain);

		if (report == null) {
			report = new TransactionReport(domain);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}
		report.getDomainNames().addAll(m_reports.keySet());
		report.accept(new StatisticsComputer());
		set95Line(report);
		return report;
	}

	private Map<Integer, AllDuration> getSortDuration(Map<Integer, AllDuration> map) {
		Map<Integer, AllDuration> result = new LinkedHashMap<Integer, AllDuration>();
		List<Entry<Integer, AllDuration>> entries = new ArrayList<Entry<Integer, AllDuration>>(map.entrySet());
		Collections.sort(entries, new Comparator<Entry<Integer, AllDuration>>() {
			@Override
			public int compare(Entry<Integer, AllDuration> o1, Entry<Integer, AllDuration> o2) {
				return o2.getKey() - o1.getKey();
			}

		});
		for (Entry<Integer, AllDuration> entry : entries) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	private void loadReports() {
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(m_startTime, "transaction");

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				TransactionReport report = DefaultSaxParser.parse(xml);

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
		report.addIp(tree.getIpAddress());

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		}
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
		}

		processTransactionGraph(name, t);
		processTransactionRange(t, type);

		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				count += processTransaction(report, tree, (Transaction) child);
			}
		}

		return count;
	}

	private void processTransactionRange(Transaction t, TransactionType type) {
		double d = t.getDurationInMicros() / 1000d;
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(t.getTimestamp());
		int min = cal.get(Calendar.MINUTE);

		Range2 range = type.findOrCreateRange2(min);

		if (!t.isSuccess()) {
			range.incFails();
		}
		range.incCount();
		range.setSum(range.getSum() + d);
	}

	private void processTransactionGraph(TransactionName name, Transaction t) {
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

	private void set95Line(TransactionReport report) {
		Collection<Machine> machines = report.getMachines().values();
		for (Machine machine : machines) {
			for (TransactionType type : machine.getTypes().values()) {
				double typeValuevalue = get95Line(type.getAllDurations());
				type.setLine95Value(typeValuevalue);
				type.setLine95Count(1);
				type.setLine95Sum(typeValuevalue);
				for (TransactionName name : type.getNames().values()) {
					double nameValue = get95Line(name.getAllDurations());
					name.setLine95Value(nameValue);
					name.setLine95Count(1);
					name.setLine95Sum(nameValue);
				}
			}
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	private TransactionReport buildTotalTransactionReport() {
		TransactionReport all = new TransactionReport(ALL);
		all.setStartTime(new Date(m_startTime));
		all.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

		TransactionReportVisitor visitor = new TransactionReportVisitor(all);

		for (TransactionReport temp : m_reports.values()) {
			all.getIps().add(temp.getDomain());
			all.getDomainNames().add(temp.getDomain());
			visitor.visitTransactionReport(temp);
		}
		return all;
	}

	private void storeReports(boolean atEnd) {
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "transaction");

			for (TransactionReport report : m_reports.values()) {
				try {
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(getDomains());

					set95Line(report);
					clearAllDuration(report);
					String xml = new TransactionReportUrlFilter().buildXml(report);
					String domain = report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.getProducer().logError(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				TransactionReport all = buildTotalTransactionReport();

				m_reports.put(ALL, all);

				for (TransactionReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = new TransactionReportUrlFilter().buildXml(report);
						String domain = report.getDomain();

						r.setName("transaction");
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);

						Task task = m_taskDao.createLocal();
						task.setCreationDate(new Date());
						task.setProducer(ip);
						task.setReportDomain(domain);
						task.setReportName("transaction");
						task.setReportPeriod(period);
						task.setStatus(1); // status todo
						m_taskDao.insert(task);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.getProducer().logError(e);
					}
				}
			}
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

	static class TransactionReportVisitor extends BaseVisitor {

		private TransactionReport m_report;

		public String m_currentDomain;

		public TransactionReportVisitor(TransactionReport report) {
			m_report = report;
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			m_currentDomain = transactionReport.getDomain();
			super.visitTransactionReport(transactionReport);
		}

		@Override
		public void visitType(TransactionType type) {
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);

			String typeName = type.getId();

			if (typeName.equals("URL") || typeName.equals("Call") || typeName.equals("PigeonCall")
			      || typeName.equals("PigeonService") || typeName.equals("Service") || typeName.equals("SQL")
			      || typeName.startsWith("Cache.") || typeName.equals("MsgProduceTried") || typeName.equals("MsgProduced")) {
				TransactionType result = machine.findOrCreateType(typeName);
				mergeType(result, type);
			}
		}

		private void mergeType(TransactionType old, TransactionType other) {
			old.setTotalCount(old.getTotalCount() + other.getTotalCount());
			old.setFailCount(old.getFailCount() + other.getFailCount());

			if (other.getMin() < old.getMin()) {
				old.setMin(other.getMin());
			}

			if (other.getMax() > old.getMax()) {
				old.setMax(other.getMax());
			}
			old.setSum(old.getSum() + other.getSum());
			old.setSum2(old.getSum2() + other.getSum2());

			old.setLine95Sum(old.getLine95Sum() + other.getLine95Sum());
			old.setLine95Count(old.getLine95Count() + other.getLine95Count());
			if (old.getLine95Count() > 0) {
				old.setLine95Value(old.getLine95Sum() / old.getLine95Count());
			}
			if (old.getTotalCount() > 0) {
				old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
				old.setAvg(old.getSum() / old.getTotalCount());
			}

			if (old.getSuccessMessageUrl() == null) {
				old.setSuccessMessageUrl(other.getSuccessMessageUrl());
			}

			if (old.getFailMessageUrl() == null) {
				old.setFailMessageUrl(other.getFailMessageUrl());
			}
		}
	}

}
