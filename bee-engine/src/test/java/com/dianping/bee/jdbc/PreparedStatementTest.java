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
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
@RunWith(JUnit4.class)
public class PreparedStatementTest extends ComponentTestCase {
	@Test
	public void testSingleQuery() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String url = "jdbc:mysql://localhost:2330/";
		String dbName = "cat";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";
		String arg = "?useServerPrepStmts=true";
		String sql = "select type, sum(failures) from transaction where domain=? and starttime=?";

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);
		Connection conn = DriverManager.getConnection(url + dbName + (arg == null ? "" : arg), userName, password);
		PreparedStatement stmt = conn.prepareStatement(sql);
		Assert.assertNotNull(stmt);
		stmt.setString(1, "MobiApi");
		stmt.setString(2, "20120822");
		ResultSet rs = stmt.executeQuery();
		Assert.assertEquals(2, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		displayResultSet(rs);
		conn.close();
	}

	private void displayResultSet(ResultSet rs) throws SQLException {
		ResultSetMetaData meta = rs.getMetaData();
		int columns = meta.getColumnCount();
		for (int column = 1; column <= columns; column++) {
			String columnName = meta.getColumnName(column);
			System.out.print(columnName + "\t");
		}
		System.out.println();
		rs.first();
		while (rs.next()) {
			for (int column = 1; column <= columns; column++) {
				System.out.print(rs.getString(column) + "\t");
			}
			System.out.println();
		}
	}
}
