package com.dianping.cat.consumer.core;

import java.util.ArrayList;
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

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.core.dal.Report;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.core.dal.Task;
import com.dianping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.consumer.transaction.TransactionReportUrlFilter;
import com.dianping.cat.consumer.transaction.TransactionStatisticsComputer;
import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class TransactionAnalyzer2 extends AbstractMessageAnalyzer<TransactionReport> implements LogEnabled {
	public static final String ID = "transaction";

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private Map<String, TransactionReport> m_reports = new HashMap<String, TransactionReport>();

	private TransactionReport buildAllTransactionReport() {
		TransactionReport all = new TransactionReport(ALL);
		TransactionReportAllBuilder visitor = new TransactionReportAllBuilder(all);

		all.setStartTime(new Date(m_startTime));
		all.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		try {
			for (TransactionReport temp : m_reports.values()) {
				all.getIps().add(temp.getDomain());
				all.getDomainNames().add(temp.getDomain());
				visitor.visitTransactionReport(temp);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return all;
	}

	private void compute95Line(TransactionReport report) {
		Collection<Machine> machines = report.getMachines().values();
		for (Machine machine : machines) {
			for (TransactionType type : machine.getTypes().values()) {
				double typeValue = compute95LineDetail(type.getAllDurations());

				type.setLine95Value(typeValue);
				
				for (TransactionName name : type.getNames().values()) {
					double nameValue = compute95LineDetail(name.getAllDurations());

					name.setLine95Value(nameValue);
				}
			}
		}
	}

	private double compute95LineDetail(Map<Integer, AllDuration> durations) {
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
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

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
		report.accept(new TransactionStatisticsComputer());
		compute95Line(report);
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
	protected void loadReports() {
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
	public void process(MessageTree tree) {
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
		// make all duration numbers less
		Integer allDuration = new Integer(((int) duration) / 5 * 5); // 5ms

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

					compute95Line(report);
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
				TransactionReport all = buildAllTransactionReport();

				m_reports.put(ALL, all);

				for (TransactionReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = new TransactionReportUrlFilter().buildXml(report);
						String domain = report.getDomain();

						r.setName(ID);
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
						task.setReportName(ID);
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
}
