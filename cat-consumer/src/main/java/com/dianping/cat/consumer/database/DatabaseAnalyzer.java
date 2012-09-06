package com.dianping.cat.consumer.database;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.entity.Domain;
import com.dianping.cat.consumer.database.model.entity.Method;
import com.dianping.cat.consumer.database.model.entity.Table;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.database.model.transform.DefaultXmlBuilder;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class DatabaseAnalyzer extends AbstractMessageAnalyzer<DatabaseReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	private Map<String, DatabaseReport> m_reports = new HashMap<String, DatabaseReport>();

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

	public DatabaseReport getReport(String domain) {
		DatabaseReport report = m_reports.get(domain);

		if (report == null) {
			report = new DatabaseReport(domain);
		}

		report.getDatabaseNames().addAll(m_reports.keySet());
		return report;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	private void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "database");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				DatabaseReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDatabase(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading database reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	@Override
	protected void process(MessageTree tree) {
		Message message = tree.getMessage();

		if (message instanceof Transaction) {
			processTransaction(tree, (Transaction) message);
		}
	}

	private void processTransaction(MessageTree tree, Transaction t) {
		String type = t.getType();

		if ("SQL".equals(type)) {
			DatabaseItem item = buildDataBaseItem(t);
			if (item != null) {
				String sqlName = t.getName();
				String domainName = tree.getDomain();
				String database = item.getDatabase();
				String tables = item.getTables();
				String method = item.getMethod();

				DatabaseReport report = m_reports.get(database);
				if (report == null) {
					report = new DatabaseReport(database);
					report.setStartTime(new Date(m_startTime));
					report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
					m_reports.put(database, report);
				}
				report.addDomainName(tree.getDomain());
				report.setConnectUrl(item.getConnectionUrl());

				Domain domain = report.findOrCreateDomain(domainName);
				Table allTable = domain.findOrCreateTable("All");
				Table table = domain.findOrCreateTable(tables);

				updateTableInfo(allTable, method, sqlName, t);
				updateTableInfo(table, method, sqlName, t);

				allTable.setTotalPercent(1);
				table.setTotalPercent(table.getTotalCount() / (double) allTable.getTotalCount());
			}
		} else {
			List<Message> messages = t.getChildren();

			for (Message message : messages) {
				if (message instanceof Transaction) {
					processTransaction(tree, (Transaction) message);
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
		m.setTotalPercent(m.getTotalCount() / (double) table.getTotalCount());
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "database");

			for (DatabaseReport report : m_reports.values()) {
				Set<String> domainNames = report.getDatabaseNames();
				domainNames.clear();
				domainNames.addAll(getDomains());

				String xml = builder.buildXml(report);
				String domain = report.getDatabase();

				reportBucket.storeById(domain, xml);
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (DatabaseReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDatabase();

						r.setName("database");
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(2);
						r.setContent(xml);

						m_reportDao.insert(r);

						// Task task = m_taskDao.createLocal();
						// task.setCreationDate(new Date());
						// task.setProducer(ip);
						// task.setReportDomain(domain);
						// task.setReportName("database");
						// task.setReportPeriod(period);
						// task.setStatus(1); // status todo
						// m_taskDao.insert(task);
						// m_logger.info("insert database task:" + task.toString());
					} catch (Throwable e) {
						Cat.getProducer().logError(e);
					}
				}
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing database reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}

	}

	private DatabaseItem buildDataBaseItem(Transaction t) {
		List<Message> messages = t.getChildren();
		String connection = null;
		String method = null;
		String sql = (String) t.getData();

		for (Message message : messages) {
			if (message instanceof Event) {
				String type = message.getType();

				if (type.equals("SQL")) {
					method = message.getName();
				} else if (type.equals("SQL.database")) {
					connection = message.getName();
				}
			}
		}
		if (connection != null && method != null) {
			DatabaseItem item = new DatabaseItem();
			String tables = getTableNamesBySql(sql);
			String database = getDataBaseName(connection);

			item.setDatabase(database).setTables(tables).setMethod(method).setConnectionUrl(connection);
			return item;
		}
		return null;
	}

	private String getTableNamesBySql(String sql) {
		try {
			if (sql == null || sql.length() == 0)
				sql = "TTT";
			return "Table" + sql.substring(0, 6);

		} catch (Exception e) {

		}
		return "Test";
	}

	private String getDataBaseName(String url) {
		try {
			int index = url.indexOf("://");
			String temp = url.substring(index + 3);
			index = temp.indexOf("/");
			int index2 = temp.indexOf("?");
			String schema = temp.substring(index + 1, index2 != -1 ? index2 : temp.length());
			return schema;
		} catch (Exception e) {
		}
		return "Unknown";
	}

	public static class DatabaseItem {
		private String m_connectionUrl;

		private String m_database;

		private String m_tables;

		private String m_method;

		public String getDatabase() {
			return m_database;
		}

		public String getConnectionUrl() {
			return m_connectionUrl;
		}

		public DatabaseItem setConnectionUrl(String connectionUrl) {
			m_connectionUrl = connectionUrl;
			return this;
		}

		public DatabaseItem setDatabase(String database) {
			m_database = database;
			return this;
		}

		public String getTables() {
			return m_tables;
		}

		public DatabaseItem setTables(String tables) {
			m_tables = tables;
			return this;
		}

		public String getMethod() {
			return m_method;
		}

		public DatabaseItem setMethod(String method) {
			m_method = method;
			return this;
		}
	}
}
