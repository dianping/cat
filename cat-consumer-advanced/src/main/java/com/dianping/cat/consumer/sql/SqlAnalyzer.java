package com.dianping.cat.consumer.sql;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.sql.model.entity.Database;
import com.dianping.cat.consumer.sql.model.entity.Method;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.entity.Table;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;

public class SqlAnalyzer extends AbstractMessageAnalyzer<SqlReport> implements LogEnabled {
	public static final String ID = "sql";
	
	@Inject(ID)
	private ReportManager<SqlReport> m_reportManager;

	@Inject
	private SqlParseManager m_sqlParseManager;

	@Inject
	private DatabaseParser m_parser;
	
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
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
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
		SqlReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		if (report == null) {
			report = new SqlReport(domain);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		return report;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE);
	}

	@Override
	public void process(MessageTree tree) {
		Message message = tree.getMessage();
		String domain = tree.getDomain();
		SqlReport report = m_reportManager.getHourlyReport(getStartTime(), domain, true);
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
