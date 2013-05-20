package com.dianping.cat.consumer.advanced;

import junit.framework.Assert;

import org.junit.Test;

public class DatabaseAnalyzerTest {
	@Test
	public void testParseDatabaseName() {
		String mysql = "jdbc:mysql://127.0.0.1:3306/cat";
		String net = "jdbc:sqlserver://10.1.1.241:1433;xopenStates=false;sendTimeAsDatetime=true;trustServerCertificate=false;sendStringParametersAsUnicode=true;selectMethod=direct;responseBuffering=adaptive;packetSize=8000;loginTimeout=15;lockTimeout=-1;lastUpdateCount=true;encrypt=false;disableStatementPooling=true;databaseName=zSurvey_NET;applicationName=Microsoft SQL Server JDBC Driver;";

		for (int i = 0; i < 100; i++) {
			String mysqlResult = DatabaseParseUtil.parseDatabaseName(mysql);
			String netResult = DatabaseParseUtil.parseDatabaseName(net);

			Assert.assertEquals("cat", mysqlResult);
			Assert.assertEquals("zSurvey_NET", netResult);

		}
	}
}
