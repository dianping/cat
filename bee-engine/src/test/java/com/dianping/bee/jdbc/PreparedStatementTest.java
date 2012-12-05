/**
 * Project: bee-engine
 * 
 * File Created at 2012-8-29
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class PreparedStatementTest {
	@Test
	public void testSingleQuery() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql = "select type, sum(failures),domain,starttime from transaction where domain=? and starttime=?";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		PreparedStatement stmt = conn.prepareStatement(sql);
		Assert.assertNotNull(stmt);
		stmt.setString(1, "MobileApi");
		stmt.setString(2, "20120822");
		ResultSet rs = stmt.executeQuery();
		Assert.assertEquals(4, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs);
		conn.close();
	}

	@Test
	public void testSingleQueryWithoutParameter() throws InstantiationException, IllegalAccessException,
	      ClassNotFoundException, SQLException {
		String sql = "select type, sum(failures) ,domain,starttime from transaction";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		PreparedStatement stmt = conn.prepareStatement(sql);
		Assert.assertNotNull(stmt);
		ResultSet rs = stmt.executeQuery();
		Assert.assertEquals(4, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs);
		conn.close();
	}
}
