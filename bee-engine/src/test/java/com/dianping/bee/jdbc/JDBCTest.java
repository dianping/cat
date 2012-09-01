/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-24
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
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(JUnit4.class)
public class JDBCTest extends ComponentTestCase {

	@Test
	public void testConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Connection conn = null;
		String url = "jdbc:mysql://127.0.0.1:2330/";
		String dbName = "cat";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";
		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		conn = DriverManager.getConnection(url + dbName, userName, password);
		Assert.assertNotNull(conn);
		Assert.assertFalse(conn.isClosed());
		conn.close();
		Assert.assertTrue(conn.isClosed());
	}

	@Test
	public void testQueryInformationSchema() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String url = "jdbc:mysql://localhost:2330/";
		String dbName = "cat";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";
		String sql = "SELECT `DEFAULT_COLLATION_NAME` FROM `information_schema`.`SCHEMATA` WHERE `SCHEMA_NAME`='cat'";

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		Connection conn = DriverManager.getConnection(url + dbName, userName, password);
		Statement stmt = conn.createStatement();
		Assert.assertNotNull(stmt);
		ResultSet rs = stmt.executeQuery(sql);
		Assert.assertEquals(1, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertEquals(0, rs.getRow());
		displayResultSet(rs);
		conn.close();

	}

	@Test
	public void testSingleQuery() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String url = "jdbc:mysql://localhost:2330/";
		String dbName = "cat";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		Connection conn = DriverManager.getConnection(url + dbName, userName, password);
		Statement stmt = conn.createStatement();
		Assert.assertNotNull(stmt);
		ResultSet rs = stmt.executeQuery("select type, sum(failures) from transaction where domain=? and starttime=?");
		Assert.assertEquals(2, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		displayResultSet(rs);
		conn.close();
	}

	@Test
	public void testMultiQueryInSameDatabase() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String url = "jdbc:mysql://localhost:2330/";
		String dbName = "cat";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		Connection conn = DriverManager.getConnection(url + dbName, userName, password);

		Statement stmt1 = conn.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery("select type, sum(failures) from transaction where domain=? and starttime=?");
		Assert.assertEquals(2, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);
		displayResultSet(rs1);

		Statement stmt2 = conn.createStatement();
		Assert.assertNotNull(stmt2);
		ResultSet rs2 = stmt2.executeQuery("select type,sum(failures) from event");
		Assert.assertEquals(2, rs2.getMetaData().getColumnCount());
		Assert.assertNotNull(rs2);
		rs2.last();
		Assert.assertTrue(rs2.getRow() > 0);
		displayResultSet(rs2);

		Statement stmt3 = conn.createStatement();
		Assert.assertNotNull(stmt3);
		ResultSet rs3 = stmt3.executeQuery("select * from transaction");
		Assert.assertTrue(rs3.getMetaData().getColumnCount() > 0);
		Assert.assertNotNull(rs3);
		rs3.last();
		Assert.assertTrue(rs3.getRow() > 0);
		displayResultSet(rs3);
		conn.close();
	}

	@Test
	public void testMultiQueryInMultiDatabaese() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String url = "jdbc:mysql://localhost:2330/";
		String dbName = "cat";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		Connection conn1 = DriverManager.getConnection(url + dbName, userName, password);

		Statement stmt1 = conn1.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery("select type, sum(failures) from transaction where domain=? and starttime=?");
		Assert.assertEquals(2, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);
		displayResultSet(rs1);
		conn1.close();

		dbName = "dog";
		Connection conn2 = DriverManager.getConnection(url + dbName, userName, password);
		Statement stmt2 = conn2.createStatement();
		Assert.assertNotNull(stmt2);
		ResultSet rs2 = stmt2.executeQuery("select type, sum(failures) from transaction where domain=? and starttime=?");
		Assert.assertEquals(2, rs2.getMetaData().getColumnCount());
		Assert.assertNotNull(rs2);
		rs2.last();
		Assert.assertTrue(rs2.getRow() > 0);
		displayResultSet(rs2);
		conn2.close();
	}

	private void displayResultSet(ResultSet rs) throws SQLException {
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
}
