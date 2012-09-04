package com.dianping.cat.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class TransactionTest extends ComponentTestCase {
	private Connection m_conn;

	@Before
	public void before() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String url = "jdbc:mysql://localhost:2330/cat?useServerPrepStmts=true";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";

		Class.forName(driver).newInstance();
		DriverManager.setLoginTimeout(600);

		m_conn = DriverManager.getConnection(url, userName, password);
	}

	@Test
	public void testQuery1() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {
		String sql = "select type from transaction where domain='Cat' and starttime is null";
		ResultSet rs = m_conn.createStatement().executeQuery(sql);

		Assert.assertNotNull(rs);
		rs.last();

		Assert.assertTrue(rs.getRow() > 0);
		displayResultSet(rs);
		m_conn.close();
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
