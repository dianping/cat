package com.dianping.cat.consumer.advanced;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.sql.DatabaseParser;

public class DatabaseParserTest {
	@Test
	public void testParseDatabaseName() {
		String mysql = "jdbc:mysql://127.0.0.1:3306/cat";
		String net = "jdbc:sqlserver://10.1.1.241:1433;xopenStates=false;sendTimeAsDatetime=true;trustServerCertificate=false;sendStringParametersAsUnicode=true;selectMethod=direct;responseBuffering=adaptive;packetSize=8000;loginTimeout=15;lockTimeout=-1;lastUpdateCount=true;encrypt=false;disableStatementPooling=true;databaseName=zSurvey_NET;applicationName=Microsoft SQL Server JDBC Driver;";
		DatabaseParser parser = new DatabaseParser();
		for (int i = 0; i < 100; i++) {
			String mysqlResult = parser.parseDatabaseName(mysql);
			String netResult = parser.parseDatabaseName(net);

			Assert.assertEquals("cat", mysqlResult);
			Assert.assertEquals("zSurvey_NET", netResult);

		}
	}
}
