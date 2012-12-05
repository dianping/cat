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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class StatementTest {
	@Test
	public void testConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		Connection conn = JDBCTestHelper.getCatConnection(null);
		Assert.assertNotNull(conn);
		Assert.assertFalse(conn.isClosed());
		conn.close();
		Assert.assertTrue(conn.isClosed());
	}

	@Test
	public void testMultiQueryInSameDatabase() throws InstantiationException, IllegalAccessException,
	      ClassNotFoundException, SQLException {
		String sql = "select type, sum(failures) from transaction where domain='MobileApi' and starttime='20120822'";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt1 = conn.createStatement();
		Assert.assertNotNull(stmt1);
		ResultSet rs1 = stmt1.executeQuery(sql);
		Assert.assertEquals(2, rs1.getMetaData().getColumnCount());
		Assert.assertNotNull(rs1);
		rs1.last();
		Assert.assertTrue(rs1.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs1);

		Statement stmt2 = conn.createStatement();
		Assert.assertNotNull(stmt2);
		sql = "select type,sum(failures) from event";
		ResultSet rs2 = stmt2.executeQuery(sql);
		Assert.assertEquals(2, rs2.getMetaData().getColumnCount());
		Assert.assertNotNull(rs2);
		rs2.last();
		Assert.assertTrue(rs2.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs2);

		Statement stmt3 = conn.createStatement();
		Assert.assertNotNull(stmt3);
		sql = "select * from transaction";
		ResultSet rs3 = stmt3.executeQuery(sql);
		Assert.assertTrue(rs3.getMetaData().getColumnCount() > 0);
		Assert.assertNotNull(rs3);
		rs3.last();
		Assert.assertTrue(rs3.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs3);
		conn.close();
	}

	@Test
	public void testSingleQuery() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql = "select `type`, sum(failures), domain from transaction where domain='MobileApi' and starttime='20120822'";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt = conn.createStatement();
		Assert.assertNotNull(stmt);
		ResultSet rs = stmt.executeQuery(sql);
		Assert.assertEquals(3, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs);
		conn.close();
	}
}
