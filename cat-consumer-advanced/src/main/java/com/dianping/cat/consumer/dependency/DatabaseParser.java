package com.dianping.cat.consumer.dependency;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseParser {

	public Map<String, String> m_connections = new ConcurrentHashMap<String, String>();

	public String parseDatabaseName(String connectionUrl) {
		String database = m_connections.get(connectionUrl);
		if (database != null) {
			return database;
		} else {
			if (connectionUrl.indexOf("mysql") > -1) {
				try {
					int index = connectionUrl.indexOf("://");
					String temp = connectionUrl.substring(index + 3);
					index = temp.indexOf("/");
					int index2 = temp.indexOf("?");
					database = temp.substring(index + 1, index2 != -1 ? index2 : temp.length());
				} catch (Exception e) {
				}
			} else if (connectionUrl.indexOf("sqlserver") > -1) {
				String temp = connectionUrl.substring(connectionUrl.indexOf("databaseName"));
				int first = temp.indexOf("=");
				int end = temp.indexOf(";");

				if (first > -1 && end > -1) {
					database = temp.substring(first + 1, end);
				}
			}
		}
		if (database != null) {
			m_connections.put(connectionUrl, database);
		}

		return database;
	}
}
