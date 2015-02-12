package com.dianping.cat.consumer.storage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;

public class DatabaseParser implements LogEnabled {

	private Logger m_logger;

	private Map<String, Database> m_connections = new LinkedHashMap<String, Database>() {

		private static final long serialVersionUID = 1L;

		@Override
		protected boolean removeEldestEntry(Entry<String, Database> eldest) {
			return size() > 50000;
		}
	};

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public Database queryDatabaseName(String connection) {
		Database database = m_connections.get(connection);

		if (database == null && StringUtils.isNotEmpty(connection)) {
			try {
				if (connection.contains("jdbc:mysql://")) {
					String con = connection.split("jdbc:mysql://")[1];
					con = con.split("\\?")[0];
					String ip = con.substring(0, con.indexOf(":"));
					String name = con.substring(con.indexOf("/") + 1);
					database = new Database(name, ip);

					m_connections.put(connection, database);
				} else if (connection.contains("jdbc:sqlserver://")) {
					String con = connection.split("jdbc:sqlserver://")[1];
					String ip = con.substring(0, con.indexOf(":"));
					String field = connection.split("DatabaseName=")[1];
					String name = field.substring(0, field.indexOf(";"));
					database = new Database(name, ip);

					m_connections.put(name, database);
				} else {
					m_logger.info("Unrecognized jdbc connection string: " + connection);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		return database;
	}

	public static class Database {

		private String m_name;

		private String m_ip;

		public Database(String name, String ip) {
			m_name = name;
			m_ip = ip;
		}

		public String getName() {
			return m_name;
		}

		public String getIp() {
			return m_ip;
		}
	}
}
