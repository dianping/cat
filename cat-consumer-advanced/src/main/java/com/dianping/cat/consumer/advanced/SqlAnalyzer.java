package com.dianping.cat.consumer.advanced;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.sql.SqlParseManager;
import com.dianping.cat.consumer.sql.model.entity.Database;
import com.dianping.cat.consumer.sql.model.entity.Method;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.entity.Table;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.sql.model.transform.DefaultXmlBuilder;
import com.dianping.cat.core.dal.HourlyReport;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class SqlAnalyzer extends AbstractMessageAnalyzer<SqlReport> implements LogEnabled {
	public static final String ID = "sql";
	
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private HourlyReportDao m_reportDao;

	@Inject
	private SqlParseManager m_sqlParseManager;

	@Inject
	private DatabaseParser m_parser;

	private Map<String, SqlReport> m_reports = new HashMap<String, SqlReport>();

	private Set<String> m_errorConnectionUrls = new HashSet<String>();

	private DatabaseItem buildDataBaseItem(String domain, Transaction t) {
		List<Message> messages = t.getChildren();
		String connection = null;
		String method = null;
		String sqlName = t.getName();
		String sqlStatement = (String) t.getData();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL.Method")) {
					method = message.getName();
				} else if (type.equals("SQL.Database")) {
					connection = message.getName();
				}
			}
		}
		if (connection != null && method != null) {
			DatabaseItem item = new DatabaseItem();
			String tables = m_sqlParseManager.getTableNames(sqlName, sqlStatement, domain);
			String database =  m_parser.parseDatabaseName(connection);

			if (database == null) {
				m_errorConnectionUrls.add(domain + ":" + connection);
				database = "unknown";
			}
			item.setDatabase(database).setTables(tables).setMethod(method).setConnectionUrl(connection);
			return item;
		}
		return null;
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);

		if (m_errorConnectionUrls.size() > 0) {
			m_logger.error(m_errorConnectionUrls.toString());
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public SqlReport getReport(String domain) {
		SqlReport report = m_reports.get(domain);

		if (report == null) {
			report = new SqlReport(domain);

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
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "sql");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				SqlReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading sql reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	@Override
	public void process(MessageTree tree) {
		Message message = tree.getMessage();
		String domain = tree.getDomain();
		SqlReport report = m_reports.get(domain);

		if (report == null) {
			report = new SqlReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}
		report.addDomainName(domain);

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		}
	}

	private void processTransaction(SqlReport report, MessageTree tree, Transaction t) {
		String type = t.getType();

		if ("SQL".equals(type)) {
			DatabaseItem item = buildDataBaseItem(tree.getDomain(), t);
			if (item != null) {
				String sqlName = t.getName();
				String databaseName = item.getDatabase();
				String tables = item.getTables();
				String method = item.getMethod();

				report.addDatabaseName(databaseName);

				Database database = report.findOrCreateDatabase(databaseName);
				database.setConnectUrl(item.getConnectionUrl());

				Table allTable = database.findOrCreateTable("All");
				Table table = database.findOrCreateTable(tables);

				updateTableInfo(allTable, method, sqlName, t);
				updateTableInfo(table, method, sqlName, t);
			}
		} else {
			List<Message> messages = t.getChildren();

			for (Message message : messages) {
				if (message instanceof Transaction) {
					processTransaction(report, tree, (Transaction) message);
				}
			}
		}
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "sql");

			for (SqlReport report : m_reports.values()) {
				try {
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(m_reports.keySet());

					String xml = builder.buildXml(report);
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

				for (SqlReport report : m_reports.values()) {
					try {
						HourlyReport r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName(ID);
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
			m_logger.error(String.format("Error when storing sql reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}

	}

	private void updateTableInfo(Table table, String method, String sqlName, Transaction t) {
		String status = t.getStatus();
		double duration = t.getDurationInMicros() / 1000d;

		Method m = table.findOrCreateMethod(method);

		if (!status.equals(Message.SUCCESS)) {
			m.incFailCount();
			table.incFailCount();
		}
		m.addSql(sqlName);

		m.incTotalCount();
		m.setSum(m.getSum() + duration);
		m.setAvg(m.getSum() / (double) m.getTotalCount());
		m.setFailPercent(m.getFailCount() / (double) m.getTotalCount());

		table.incTotalCount();
		table.setSum(table.getSum() + duration);
		table.setAvg(table.getSum() / (double) table.getTotalCount());
		table.setFailPercent(table.getFailCount() / (double) table.getTotalCount());
	}

	public static class DatabaseItem {
		private String m_connectionUrl;

		private String m_database;

		private String m_tables;

		private String m_method;

		public String getConnectionUrl() {
			return m_connectionUrl;
		}

		public String getDatabase() {
			return m_database;
		}

		public String getMethod() {
			return m_method;
		}

		public String getTables() {
			return m_tables;
		}

		public DatabaseItem setConnectionUrl(String connectionUrl) {
			m_connectionUrl = connectionUrl;
			return this;
		}

		public DatabaseItem setDatabase(String sql) {
			m_database = sql;
			return this;
		}

		public DatabaseItem setMethod(String method) {
			m_method = method;
			return this;
		}

		public DatabaseItem setTables(String tables) {
			m_tables = tables;
			return this;
		}
	}
}
