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
	public void testConnection() {
		Connection conn = null;
		String url = "jdbc:mysql://localhost:2330/";
		String dbName = "cat";
		String driver = "com.mysql.jdbc.Driver";
		String userName = "test";
		String password = "test";
		try {
			Class.forName(driver).newInstance();
			System.out.println("Driver loaded");
			DriverManager.setLoginTimeout(600);
			conn = DriverManager.getConnection(url + dbName, userName, password);
			System.out.println("Connected to the database");
			conn.close();
			System.out.println("Disconnected from database");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
