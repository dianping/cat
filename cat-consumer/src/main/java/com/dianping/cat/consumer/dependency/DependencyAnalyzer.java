package com.dianping.cat.consumer.dependency;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.report.ReportManager;

public class DependencyAnalyzer extends AbstractMessageAnalyzer<DependencyReport> implements LogEnabled {
	public static final String ID = "dependency";

	@Inject(ID)
	private ReportManager<DependencyReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Inject
	private DatabaseParser m_parser;

	private Set<String> m_types = new HashSet<String>(Arrays.asList("URL", "SQL", "Call", "PigeonCall", "Service",
	      "PigeonService"));

	private Set<String> m_exceptions = new HashSet<String>(Arrays.asList("Exception", "RuntimeException", "Error"));

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

	private DependencyReport findOrCreateReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, true);
	}

	@Override
	public int getAnalyzerCount() {
		return 2;
	}

	@Override
	public DependencyReport getReport(String domain) {
		DependencyReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		return report;
	}

	@Override
	public ReportManager<DependencyReport> getReportManager() {
		return m_reportManager;
	}

	private boolean isCache(String type) {
		return type.startsWith("Cache.");
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
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

	private String parseServerName(Transaction t) {
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				if (message.getType().equals("PigeonCall.app")) {
					return message.getName();
				}
			}
		}
		return null;
	}

	@Override
	public void process(MessageTree tree) {
		DependencyReport report = findOrCreateReport(tree.getDomain());
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
			Index index = segment.findOrCreateIndex("Exception");

			index.incTotalCount();
			index.incErrorCount();
		}
	}

	private void processPigeonTransaction(DependencyReport report, MessageTree tree, Transaction t, String type) {
		if ("PigeonCall".equals(type) || "Call".equals(type)) {
			String target = parseServerName(t);
			String callType = "PigeonCall";

			if (target != null) {
				updateDependencyInfo(report, t, target, callType);
				DependencyReport serverReport = findOrCreateReport(target);

				updateDependencyInfo(serverReport, t, tree.getDomain(), "PigeonService");
			}
		}
	}

	private void processSqlTransaction(DependencyReport report, Transaction t, String type) {
		if ("SQL".equals(type)) {
			String database = parseDatabase(t);

			if (database != null) {
				updateDependencyInfo(report, t, database, "Database");
			}
		}
	}

	private void processTransaction(DependencyReport report, MessageTree tree, Transaction t) {
		String type = t.getType();
		String name = t.getName();

		if (m_serverFilterConfigManager.discardTransaction(type, name)) {
			return;
		} else {
			processTransactionType(report, t, type);
			processSqlTransaction(report, t, type);
			processPigeonTransaction(report, tree, t, type);

			List<Message> children = t.getChildren();

			for (Message child : children) {
				if (child instanceof Transaction) {
					processTransaction(report, tree, (Transaction) child);
				} else if (child instanceof Event) {
					processEvent(report, tree, (Event) child);
				}
			}
		}
	}

	private void processTransactionType(DependencyReport report, Transaction t, String type) {
		if (m_types.contains(type) || isCache(type)) {
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

		if (isCache(type)) {
			updateDependencyInfo(report, t, type, "Cache");
		}
	}

	private void updateDependencyInfo(DependencyReport report, Transaction t, String target, String type) {
		synchronized (report) {
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

}
