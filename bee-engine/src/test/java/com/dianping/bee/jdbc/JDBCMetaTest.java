/**
 * Project: bee-engine
 * 
 * File Created at 2012-9-13
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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class JDBCMetaTest {

	@Test
	public void testGetTables() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		Connection conn = JDBCTestHelper.getCatConnection(null);
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getTables(null, null, "%", null);
		Assert.assertEquals(5, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet("tables", rs);
	}

	@Test
	public void testGetColumns() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		Connection conn = JDBCTestHelper.getCatConnection(null);
		DatabaseMetaData meta = conn.getMetaData();
		ResultSet rs = meta.getColumns(null, null, "transaction", "%");
		Assert.assertEquals(23, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet("columns", rs);
	}
}
