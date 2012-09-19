package com.dianping.cat.consumer.transaction;

import java.io.IOException;
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
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
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
		return m_reports.keySet();
	}

	@Override
	public TransactionReport getReport(String domain) {
		TransactionReport report = m_reports.get(domain);

		if (report == null) {
			report = new TransactionReport(domain);
		}
		// report.accept(new TransactionReportFilter());
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
			int count = processTransaction(report, tree, (Transaction) message);

			if (count > 0) {
				storeMessage(tree);
			}
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
		// DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "transaction");

			for (TransactionReport report : m_reports.values()) {
				Set<String> domainNames = report.getDomainNames();
				domainNames.clear();
				domainNames.addAll(getDomains());

				set95Line(report);
				clearAllDuration(report);
				String xml = new TransactionReportFilter(50).buildXml(report);
				String domain = report.getDomain();

				reportBucket.storeById(domain, xml);
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (TransactionReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = new TransactionReportFilter(50).buildXml(report);
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
						Cat.getProducer().logError(e);
					}
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

	public static class TransactionReportFilter extends
	      com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder {
		private String m_domain;

		private int m_maxItems;

		public TransactionReportFilter(int maxItem) {
			m_maxItems = maxItem;
		}

		@Override
		public void visitType(TransactionType type) {
			long totalCount = type.getTotalCount();
			int value = (int) (totalCount / 10000);
			int count = 0;
			String successMessageUrl = null;

			value = Math.min(value, 5);
			if (!"Cat".equals(m_domain) && (value > 0)) {
				if ("URL".equals(type.getId())) {
					List<String> names = new ArrayList<String>();
					Map<String, TransactionName> transactionNames = type.getNames();
					if (transactionNames.size() > m_maxItems) {
						for (TransactionName transactionName : transactionNames.values()) {
							if (transactionName.getTotalCount() <= value) {
								names.add(transactionName.getId());
								count += transactionName.getTotalCount();
								if (successMessageUrl == null) {
									successMessageUrl = transactionName.getSuccessMessageUrl();
								}
							}
						}

						for (String name : names) {
							transactionNames.remove(name);
						}
						if (count > 0) {
							TransactionName name = new TransactionName("OTHERS");
							name.setSuccessMessageUrl(successMessageUrl);
							name.setTotalCount(count);
							name.setMin(0);
							name.setMax(0);
							type.getNames().put("OTHERS", name);
						}
					}
				}
			}
			super.visitType(type);
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			m_domain = transactionReport.getDomain();
			super.visitTransactionReport(transactionReport);
		}
	}
}
