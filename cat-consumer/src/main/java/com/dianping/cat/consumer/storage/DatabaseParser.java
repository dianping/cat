package com.dianping.cat.consumer.storage;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;

public class DatabaseParser implements LogEnabled {

	private Logger m_logger;

	private Set<String> m_errorConnections = new HashSet<String>();

	private Map<String, Database> m_connections = new LinkedHashMap<String, Database>();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public Database queryDatabaseName(String connection) {
		Database database = m_connections.get(connection);

		if (database == null && StringUtils.isNotEmpty(connection) && !m_errorConnections.contains(connection)) {
			try {
				if (connection.contains("jdbc:mysql://")) {
					String con = connection.split("jdbc:mysql://")[1];
					con = con.split("\\?")[0];
					int index = con.indexOf(":");
					String ip = "";

					if (index < 0) {
						ip = con.split("/")[0];
					} else {
						ip = con.substring(0, index);
					}

					String name = con.substring(con.indexOf("/") + 1);
					database = new Database(name, ip);

					m_connections.put(connection, database);
				} else {
					m_errorConnections.add(connection);
					m_logger.info("Unrecognized jdbc connection string: " + connection);
				}
			} catch (Exception e) {
				m_errorConnections.add(connection);
				Cat.logError(connection, e);
			}
		}
		return database;
	}

	public void showErrorCon() {
		if (!m_connections.isEmpty()) {
			Transaction t = Cat.newTransaction("Connection", "Error");

			for (String con : m_errorConnections) {
				Cat.logEvent("Connection", "Error", Event.SUCCESS, con);
			}
			t.setStatus(Transaction.SUCCESS);
			t.complete();
		}
	}

	public static class Database {

		private String m_name;

		private String m_ip;

		public Database(String name, String ip) {
			m_name = name;
			m_ip = ip;
		}

		public String getIp() {
			return m_ip;
		}

		public String getName() {
			return m_name;
		}
	}
}
