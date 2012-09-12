/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-11
 * 
 * Copyright 2012 dianping.com.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Dianping Company. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with dianping.com.
 */
package com.dianping.bee.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Properties;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class JDBCTestHelper {

	private static boolean isPrint = true;

	public static void displayResultSet(String sql, ResultSet rs) throws SQLException {
		if (isPrint) {
			System.out.println("Query: " + sql);
		}
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();
		for (int column = 1; column <= columns; column++) {
			String columnName = meta.getColumnName(column);
			if (isPrint) {
				System.out.print(columnName + "\t");
			}
		}

		if (isPrint) {
			System.out.println();
		}
		rs.beforeFirst();

		while (rs.next()) {
			for (int column = 1; column <= columns; column++) {
				if (isPrint) {
					System.out.print(rs.getString(column) + "\t");
				}
			}
			if (isPrint) {
				System.out.println();
			}
		}
	}

	public static Connection getCatConnection(Properties props) throws InstantiationException, IllegalAccessException,
	      ClassNotFoundException, SQLException {
		if (props == null) {
			props = new Properties();
		}

		String url = props.getProperty("url") == null ? "jdbc:mysql://localhost:2330/" : props.getProperty("url");
		String db = props.getProperty("db") == null ? "cat" : props.getProperty("db");
		String driver = "com.mysql.jdbc.Driver";

		if (props.getProperty("user") == null) {
			props.setProperty("user", "bee");
		}
		if (props.getProperty("password") == null) {
			props.setProperty("password", "beebee");
		}
		if (props.getProperty("useServerPrepStmts") == null) {
			props.setProperty("useServerPrepStmts", "true");
		}
		if (props.getProperty("useUnicode") == null) {
			props.setProperty("useUnicode", "true");
		}

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);

		Connection conn = DriverManager.getConnection(url + db, props);
		return conn;
	}

	public static Connection getMySQLConnection(Properties props) throws InstantiationException, IllegalAccessException,
	      ClassNotFoundException, SQLException {
		if (props == null) {
			props = new Properties();
		}

		String url = props.getProperty("url") == null ? "jdbc:mysql://localhost:3306/" : props.getProperty("url");
		String db = props.getProperty("db") == null ? "cdcol" : props.getProperty("db");
		String driver = "com.mysql.jdbc.Driver";

		if (props.getProperty("user") == null) {
			props.setProperty("user", "root");
		}
		if (props.getProperty("password") == null) {
			props.setProperty("password", "");
		}
		if (props.getProperty("useServerPrepStmts") == null) {
			props.setProperty("useServerPrepStmts", "true");
		}
		if (props.getProperty("useUnicode") == null) {
			props.setProperty("useUnicode", "true");
		}

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		Connection conn = DriverManager.getConnection(url + db, props);
		return conn;
	}
}
