package com.dianping.cat.consumer.advanced;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.DomainManager;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.dependency.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class DependencyAnalyzer extends AbstractMessageAnalyzer<DependencyReport> implements LogEnabled {
	public static final String ID = "dependency";

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private DomainManager m_domainManager;
	
	@Inject
	private DatabaseParser m_parser;

	private Map<String, DependencyReport> m_reports = new HashMap<String, DependencyReport>();

	private Set<String> m_types = new HashSet<String>(Arrays.asList("URL", "SQL", "Call", "PigeonCall", "Service",
	      "PigeonService"));

	private Set<String> m_exceptions = new HashSet<String>(Arrays.asList("Exception", "RuntimeException", "Error"));

	private static final String UNKNOWN = "UnknownIp";

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
	public DependencyReport getReport(String domain) {
		DependencyReport report = m_reports.get(domain);

		if (report == null) {
			report = new DependencyReport(domain);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}

		report.getDomainNames().addAll(m_reports.keySet());
		return report;
	}

	@Override
	protected void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "dependency");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				DependencyReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading dependency reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	private String parseDatabase(Transaction t) {
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL.Database")) {
					return m_parser.parseDatabaseName(message.getName());
				}
			}
		}
		return null;
	}

	private String parseIpFromPigeonClientTransaction(Transaction t, MessageTree tree) {
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				if (message.getType().equals("PigeonCall.server")) {
					String name = message.getName();
					int index = name.indexOf(":");

					if (index > 0) {
						name = name.substring(0, index);
					}
					return name;
				}
			}
		}
		return UNKNOWN;
	}

	private String parseIpFromPigeonServerTransaction(Transaction t, MessageTree tree) {
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				if (message.getType().equals("PigeonService.client")) {
					String name = message.getName();
					int index = name.indexOf(":");

					if (index > 0) {
						name = name.substring(0, index);
					}
					return name;
				}
			}
		}
		MessageId id = MessageId.parse(tree.getMessageId());

		return id.getIpAddress();
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		DependencyReport report = m_reports.get(domain);

		if (report == null) {
			report = new DependencyReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		} else if (message instanceof Event) {
			processEvent(report, tree, (Event) message);
		}
	}

	private void processEvent(DependencyReport report, MessageTree tree, Event event) {
		String type = event.getType();

		if (m_exceptions.contains(type)) {
			long current = event.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateSegment(min);

			segment.incExceptionCount();
		}
	}

	private void processPigeonTransaction(DependencyReport report, MessageTree tree, Transaction t) {
		String type = t.getType();

		if ("PigeonCall".equals(type) || "Call".equals(type)) {
			String ip = parseIpFromPigeonClientTransaction(t, tree);
			String domain = m_domainManager.getDomainByIp(ip);
			String callType = "PigeonClient";

			updateDependencyInfo(report, t, domain, callType);
		} else if ("PigeonService".equals(type) || "Service".equals(type)) {
			String ip = parseIpFromPigeonServerTransaction(t, tree);
			String domain = m_domainManager.getDomainByIp(ip);

			String callType = "PigeonServer";
			updateDependencyInfo(report, t, domain, callType);
		}
	}

	private void processSqlTransaction(DependencyReport report, Transaction t) {
		String type = t.getType();

		if ("SQL".equals(type)) {
			String database = parseDatabase(t);

			if (database != null) {
				updateDependencyInfo(report, t, database, "Database");
			}
		}
	}

	private void processTransaction(DependencyReport report, MessageTree tree, Transaction t) {
		if (shouldDiscard(t)) {
			return;
		}
		processTransactionType(report, t);
		processSqlTransaction(report, t);
		processPigeonTransaction(report, tree, t);

		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child);
			} else if (child instanceof Event) {
				processEvent(report, tree, (Event) child);
			}
		}
	}

	private void processTransactionType(DependencyReport report, Transaction t) {
		String type = t.getType();

		if (m_types.contains(type) || type.startsWith("Cache.")) {
			long current = t.getTimestamp() / 1000 / 60;
			int min = (int) (current % (60));
			Segment segment = report.findOrCreateSegment(min);
			Index index = segment.findOrCreateIndex(type);

			if (!t.getStatus().equals(Transaction.SUCCESS)) {
				index.incErrorCount();
			}
			index.incTotalCount();
			index.setSum(index.getSum() + t.getDurationInMillis());
			index.setAvg(index.getSum() / index.getTotalCount());
		}
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "dependency");

			for (DependencyReport report : m_reports.values()) {
				try {
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(getDomains());

					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (DependencyReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName("dependency");
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.getProducer().logError(e);
					}
				}
			}
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing dependency reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	private void updateDependencyInfo(DependencyReport report, Transaction t, String target, String type) {
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		Segment segment = report.findOrCreateSegment(min);
		Dependency dependency = segment.findOrCreateDependency(type + ":" + target);

		dependency.setType(type);
		dependency.setTarget(target);

		if (!t.getStatus().equals(Transaction.SUCCESS)) {
			dependency.incErrorCount();
		}
		dependency.incTotalCount();
		dependency.setSum(dependency.getSum() + t.getDurationInMillis());
		dependency.setAvg(dependency.getSum() / dependency.getTotalCount());
	}
}
