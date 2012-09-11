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
	public static void displayResultSet(String sql, ResultSet rs) throws SQLException {
		System.out.println("Query: " + sql);
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();
		for (int column = 1; column <= columns; column++) {
			String columnName = meta.getColumnName(column);
			System.out.print(columnName + "\t");
		}

		System.out.println();
		rs.beforeFirst();

		while (rs.next()) {
			for (int column = 1; column <= columns; column++) {
				System.out.print(rs.getString(column) + "\t");
			}
			System.out.println();
		}
	}

	public static Connection getCatConnection(Properties props) throws InstantiationException, IllegalAccessException,
	      ClassNotFoundException, SQLException {
		String url = "jdbc:mysql://localhost:2330/";
		String db = "cat";
		String driver = "com.mysql.jdbc.Driver";

		if (props == null) {
			props = new Properties();
		}
		props = new Properties();
		if (props.getProperty("user") == null) {
			props.setProperty("user", "bee");
		}
		if (props.getProperty("password") == null) {
			props.setProperty("password", "beebee");
		}
		if (props.getProperty("useServerPrepStmts") == null) {
			props.setProperty("useServerPrepStmts", "true");
		}

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		Connection conn = DriverManager.getConnection(url + db, props);
		return conn;
	}
}
