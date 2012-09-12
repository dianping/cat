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
package com.dianping.bee.mysql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.bee.jdbc.JDBCTestHelper;

/**
 * @author <a href="mailto:yiming.liu@dianping.com">Yiming Liu</a>
 */
public class InformationSchemaTest {

	@Test
	public void testDatabaseMeta() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		 Connection conn = JDBCTestHelper.getCatConnection(null);
//		Connection conn = JDBCTestHelper.getMySQLConnection(null);
		DatabaseMetaData metadata = conn.getMetaData();
		ResultSet databaseRs = metadata.getCatalogs();
		JDBCTestHelper.displayResultSet("metadata.getCatalogs()", databaseRs);

		String[] tableTypes = new String[] { "TABLE", "SYSTEM TABLE" };
		databaseRs.beforeFirst();
		while (databaseRs.next()) {
			String database = databaseRs.getString(1);
			ResultSet tablesRs = metadata.getTables(null, database, "%", tableTypes);
			JDBCTestHelper.displayResultSet("metadata.getTables() for " + database, tablesRs);

			tablesRs.beforeFirst();
			while (tablesRs.next()) {
				String table = tablesRs.getString(3);
				ResultSet columnsRs = metadata.getColumns(null, database, table, "%");
				JDBCTestHelper.displayResultSet("meta.getColumns() for " + table, columnsRs);
			}
		}

		conn.close();
	}

	@Test
	public void testColumns() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql = "SELECT  `TABLE_CATALOG`,  `TABLE_SCHEMA`,  `TABLE_NAME`,  `COLUMN_NAME`,  `ORDINAL_POSITION`,  `COLUMN_DEFAULT`,  `IS_NULLABLE`,  `DATA_TYPE`,  `CHARACTER_MAXIMUM_LENGTH`,  `CHARACTER_OCTET_LENGTH`,  `NUMERIC_PRECISION`,  `NUMERIC_SCALE`,  `CHARACTER_SET_NAME`,  `COLLATION_NAME`,  `COLUMN_TYPE`,  `COLUMN_KEY`,  `EXTRA`,  `PRIVILEGES`,  `COLUMN_COMMENT` FROM `information_schema`.`COLUMNS` LIMIT 1000";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt = conn.createStatement();
		Assert.assertNotNull(stmt);
		ResultSet rs = stmt.executeQuery(sql);
		Assert.assertEquals(19, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs);

		String sql2 = "SELECT * FROM `information_schema`.`COLUMNS` WHERE TABLE_NAME='transaction'";
		Statement stmt2 = conn.createStatement();
		ResultSet rs2 = stmt2.executeQuery(sql2);
		JDBCTestHelper.displayResultSet(sql2, rs2);
		conn.close();
	}

	@Test
	public void testSchemata() throws InstantiationException, IllegalAccessException, ClassNotFoundException,
	      SQLException {
		String sql = "SELECT  `CATALOG_NAME`,  `SCHEMA_NAME`,  `DEFAULT_CHARACTER_SET_NAME`,  `DEFAULT_COLLATION_NAME`,  `SQL_PATH` FROM `information_schema`.`SCHEMATA` LIMIT 1000";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt = conn.createStatement();
		Assert.assertNotNull(stmt);
		ResultSet rs = stmt.executeQuery(sql);
		Assert.assertEquals(5, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs);

		String sql2 = "SELECT * FROM `information_schema`.`SCHEMATA` LIMIT 1000";
		Statement stmt2 = conn.createStatement();
		ResultSet rs2 = stmt2.executeQuery(sql2);
		Assert.assertEquals(rs.getMetaData().getColumnCount(), rs2.getMetaData().getColumnCount());
		rs2.last();
		rs.last();
		Assert.assertEquals(rs.getRow(), rs2.getRow());
		JDBCTestHelper.displayResultSet(sql2, rs2);
		conn.close();
	}

	@Test
	public void testTables() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String sql = "SELECT  `TABLE_CATALOG`,  `TABLE_SCHEMA`,  `TABLE_NAME`,  `TABLE_TYPE`,  `ENGINE`,  `VERSION`,  `ROW_FORMAT`,  `TABLE_ROWS`,  `AVG_ROW_LENGTH`,  `DATA_LENGTH`,  `MAX_DATA_LENGTH`,  `INDEX_LENGTH`,  `DATA_FREE`,  `AUTO_INCREMENT`,  `CREATE_TIME`,  `UPDATE_TIME`,  `CHECK_TIME`,  `TABLE_COLLATION`,  `CHECKSUM`,  `CREATE_OPTIONS`,  `TABLE_COMMENT` FROM `information_schema`.`TABLES` LIMIT 1000";

		Connection conn = JDBCTestHelper.getCatConnection(null);
		Statement stmt = conn.createStatement();
		Assert.assertNotNull(stmt);
		ResultSet rs = stmt.executeQuery(sql);
		Assert.assertEquals(21, rs.getMetaData().getColumnCount());
		Assert.assertNotNull(rs);
		rs.last();
		Assert.assertTrue(rs.getRow() > 0);
		JDBCTestHelper.displayResultSet(sql, rs);
		conn.close();
	}
}
