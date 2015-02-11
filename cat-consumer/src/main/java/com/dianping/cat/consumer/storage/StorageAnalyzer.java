package com.dianping.cat.consumer.storage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.Domain;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;

public class StorageAnalyzer extends AbstractMessageAnalyzer<StorageReport> implements LogEnabled {

	@Inject
	private StorageDelegate m_storageDelegate;

	@Inject(ID)
	private ReportManager<StorageReport> m_reportManager;

	public static final String ID = "storage";

	private static final long LONG_THRESHOLD = 1000;

	private Map<String, Pair<String, String>> m_connections = new LinkedHashMap<String, Pair<String, String>>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, Pair<String, String>> eldest) {
			return size() > 5000;
		}

	};

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

	@Override
	public StorageReport getReport(String id) {
		long period = getStartTime();
		StorageReport report = m_reportManager.getHourlyReport(period, id, false);
		String type = id.substring(id.lastIndexOf("-") + 1);

		for (String myId : m_reportManager.getDomains(period)) {
			if (myId.endsWith(type)) {
				String prefix = myId.substring(0, myId.lastIndexOf("-"));

				report.getIds().add(prefix);
			}
		}
		return report;
	}

	@Override
	protected void process(MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			Transaction root = (Transaction) message;

			processTransaction(tree, root);
		}

	}

	private void processCacheTransaction(MessageTree tree, Transaction t) {
		String ip = "";
		String domain = tree.getDomain();
		String cacheName = t.getType().substring(6);
		String value = t.getName();
		String method = value.substring(value.lastIndexOf(":") + 1);
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("Cache.memcached.server")) {
					ip = message.getName();
					int index = ip.indexOf(":");

					if (index > -1) {
						ip = ip.substring(0, index);
					}
				}
			}
		}
		if (StringUtils.isNotEmpty(method) && StringUtils.isNotEmpty(ip)) {
			String id = queryCacheId(cacheName);

			updateStorageReport(id, method, ip, domain, t);
		}
	}

	private void processSQLTransaction(MessageTree tree, Transaction t) {
		String databaseName = "";
		String method = "";
		String ip = "";
		String domain = tree.getDomain();
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL.Method")) {
					method = message.getName().toLowerCase();
				}
				if (type.equals("SQL.Database")) {
					Pair<String, String> pair = queryDatabaseName(message.getName());

					if (pair != null) {
						ip = pair.getKey();
						databaseName = pair.getValue();
					}
				}
			}
		}
		if (StringUtils.isNotEmpty(databaseName) && StringUtils.isNotEmpty(method) && StringUtils.isNotEmpty(ip)) {
			String id = querySQLId(databaseName);

			updateStorageReport(id, method, ip, domain, t);
		}
	}

	private void processTransaction(MessageTree tree, Transaction t) {
		if (m_serverConfigManager.discardTransaction(t)) {
			return;
		} else {
			String type = t.getType();

			if (m_serverConfigManager.isSQLTransaction(type)) {
				processSQLTransaction(tree, t);
			} else if (m_serverConfigManager.isCacheTransaction(type)) {
				processCacheTransaction(tree, t);
			}
		}
		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(tree, (Transaction) child);
			}
		}
	}

	private String queryCacheId(String name) {
		return name + "-Cache";
	}

	private Pair<String, String> queryDatabaseName(String name) {
		Pair<String, String> pair = m_connections.get(name);

		if (pair == null && StringUtils.isNotEmpty(name)) {
			try {
				if (name.contains("jdbc:mysql://")) {
					String con = name.split("jdbc:mysql://")[1];
					con = con.split("\\?")[0];
					String ip = con.substring(0, con.indexOf(":"));
					String database = con.substring(con.indexOf("/") + 1);
					pair = new Pair<String, String>(ip, database);

					m_connections.put(name, pair);
				} else if (name.contains("jdbc:sqlserver://")) {
					String con = name.split("jdbc:sqlserver://")[1];
					String ip = con.substring(0, con.indexOf(":"));
					String field = name.split("DatabaseName=")[1];
					String database = field.substring(0, field.indexOf(";"));
					pair = new Pair<String, String>(ip, database);

					m_connections.put(name, pair);
				} else {
					Cat.logError(new RuntimeException("Unrecognized jdbc connection string: " + name));
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return pair;
	}

	private String querySQLId(String name) {
		return name + "-SQL";
	}

	private void updateStorageReport(String id, String method, String ip, String domain, Transaction t) {
		StorageReport report = m_reportManager.getHourlyReport(getStartTime(), id, true);
		Domain d = report.findOrCreateMachine(ip).findOrCreateDomain(domain);
		long current = t.getTimestamp() / 1000 / 60;
		int min = (int) (current % (60));
		Operation operation = d.findOrCreateOperation(method);
		Segment segment = operation.findOrCreateSegment(min);
		long duration = t.getDurationInMillis();

		report.addIp(ip);

		operation.incCount();
		operation.incSum(duration);
		operation.setAvg(operation.getSum() / operation.getCount());

		segment.incCount();
		segment.incSum(duration);
		segment.setAvg(segment.getSum() / segment.getCount());

		if (!t.isSuccess()) {
			operation.incError();
			segment.incError();
		}
		if (duration > LONG_THRESHOLD) {
			operation.incLongCount();
			segment.incLongCount();
		}
	}
}
