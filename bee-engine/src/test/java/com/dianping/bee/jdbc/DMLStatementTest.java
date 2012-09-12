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
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class DMLStatementTest {

	@Test
	public void testShowColumns() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql1 = "SHOW FULL COLUMNS FROM `event` FROM `cat` LIKE '%'";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt1 = conn.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery(sql1);
		Assert.assertEquals(9, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql1, rs1);

		String sql2 = "SHOW COLUMNS FROM `event` FROM `cat` LIKE '%'";

		Statement stmt2 = conn.createStatement();
		ResultSet rs2 = stmt2.executeQuery(sql2);
		Assert.assertEquals(6, rs2.getMetaData().getColumnCount());
		Assert.assertNotNull(rs2);
		rs2.last();
		Assert.assertTrue(rs2.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql2, rs2);

		String sql3 = "SHOW COLUMNS FROM `event` LIKE '%'";

		Statement stmt3 = conn.createStatement();
		ResultSet rs3 = stmt3.executeQuery(sql3);
		Assert.assertEquals(6, rs3.getMetaData().getColumnCount());
		Assert.assertNotNull(rs3);
		rs3.last();
		Assert.assertTrue(rs3.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql3, rs3);
		conn.close();
	}

	@Test
	public void testShowIndex() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql1 = "SHOW INDEX FROM `event` FROM `cat` LIKE '%'";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt1 = conn.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery(sql1);
		Assert.assertEquals(13, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);

		conn.close();
	}

	@Test
	public void testShowIndexes() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql1 = "SHOW INDEXES FROM `event` FROM `cat` LIKE '%'";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt1 = conn.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery(sql1);
		Assert.assertEquals(13, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);

		conn.close();
	}

	@Test
	public void testShowKeys() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql1 = "SHOW KEYS FROM `event` FROM `cat` LIKE '%'";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt1 = conn.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery(sql1);
		Assert.assertEquals(13, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);

		String sql2 = "SHOW KEYS FROM `event` LIKE '%'";

		Statement stmt2 = conn.createStatement();
		Assert.assertNotNull(stmt2);
		ResultSet rs2 = stmt1.executeQuery(sql2);
		Assert.assertEquals(13, rs2.getMetaData().getColumnCount());
		Assert.assertNotNull(rs2);
		rs2.last();
		Assert.assertTrue(rs2.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql2, rs2);

		conn.close();
	}

	@Test
	public void test() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		Connection conn = JDBCTestHelper.getCatConnection(null);
		DatabaseMetaData meta = conn.getMetaData();

		JDBCTestHelper.displayResultSet("tables", meta.getTables(null, null, "%", null));
		JDBCTestHelper.displayResultSet("columns", meta.getColumns(null, null, "transaction", "%"));
	}
	
	@Test
	public void testShowTables() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql1 = "SHOW FULL TABLES FROM `cat` LIKE '%'";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt1 = conn.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery(sql1);
		Assert.assertEquals(2, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql1, rs1);

		String sql2 = "SHOW TABLES FROM `cat` LIKE '%'";

		Statement stmt2 = conn.createStatement();
		ResultSet rs2 = stmt2.executeQuery(sql2);
		Assert.assertEquals(1, rs2.getMetaData().getColumnCount());
		Assert.assertNotNull(rs2);
		rs2.last();
		Assert.assertTrue(rs2.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql2, rs2);

		String sql3 = "SHOW TABLES LIKE '%'";

		Statement stmt3 = conn.createStatement();
		ResultSet rs3 = stmt3.executeQuery(sql3);
		Assert.assertEquals(1, rs3.getMetaData().getColumnCount());
		Assert.assertNotNull(rs3);
		rs3.last();
		Assert.assertTrue(rs3.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql3, rs3);

		conn.close();
	}
}
