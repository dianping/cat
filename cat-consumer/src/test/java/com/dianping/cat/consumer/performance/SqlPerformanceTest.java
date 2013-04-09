package com.dianping.cat.consumer.performance;

import org.junit.Test;

import com.dianping.cat.consumer.sql.SqlAnalyzer;
import com.dianping.cat.message.CatTestCase;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.internal.MockMessageBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;

public class SqlPerformanceTest extends CatTestCase {

	@Test
	public void test() throws Exception {
		SqlAnalyzer analyzer = lookup(SqlAnalyzer.class);
		MessageTree tree = buildMessage();

		long current = System.currentTimeMillis();

		int size = 10000000;
		for (int i = 0; i < size; i++) {
			analyzer.process(tree);
		}
		System.out.println(analyzer.getReport("cat"));
		System.out.println("Cost " + (System.currentTimeMillis() - current) / 1000);
		// cost 25
	}

	public MessageTree buildMessage() {
		Message message = new MockMessageBuilder() {
			@Override
			public MessageHolder define() {
				TransactionHolder t = t("URL", "GET", 112819)
				      .child(
				            t("SQL", "UserBlackList.findCountByUserID",
				                  "SELECT Count(*) FROM DP_UserBlackList WHERE UserID=?", 100).child(
				                  e("SQL.Method", "Select", "Execute[34796272]")).child(
				                  e("SQL.Database", "jdbc:mysql://10.1.1.63:3306/DianPingCOMM1?characterEncoding=UTF8",
				                        "Execute[34796272]")))
				      .child(
				            t("SQL", "UserBlackList.findCountByUserID1",
				                  "SELECT Count(*) FROM DP_UserBlackList WHERE UserID=?", 100).child(
				                  e("SQL.Method", "Select", "Execute[34796272]")).child(
				                  e("SQL.Database", "jdbc:mysql://10.1.1.63:3306/DianPingCOMM2?characterEncoding=UTF8",
				                        "Execute[34796272]")))
				      .child(
				            t("SQL", "UserBlackList.findCountByUserID2",
				                  "SELECT Count(*) FROM DP_UserBlackList WHERE UserID=?", 100).child(
				                  e("SQL.Method", "Select", "Execute[34796272]")).child(
				                  e("SQL.Database", "jdbc:mysql://10.1.1.63:3306/DianPingCOMM3?characterEncoding=UTF8",
				                        "Execute[34796272]")))
				      .child(
				            t("SQL", "UserBlackList.findCountByUserID3",
				                  "SELECT Count(*) FROM DP_UserBlackList WHERE UserID=?", 100).child(
				                  e("SQL.Method", "Select", "Execute[34796272]")).child(
				                  e("SQL.Database", "jdbc:mysql://10.1.1.63:3306/DianPingCOMM4?characterEncoding=UTF8",
				                        "Execute[34796272]")))
				      .child(
				            t("SQL", "UserBlackList.findCountByUserID4",
				                  "SELECT Count(*) FROM DP_UserBlackList WHERE UserID=?", 100).child(
				                  e("SQL.Method", "Select", "Execute[34796272]")).child(
				                  e("SQL.Database", "jdbc:mysql://10.1.1.63:3306/DianPingCOMM5?characterEncoding=UTF8",
				                        "Execute[34796272]")))
				      .child(
				            t("SQL", "UserBlackList.findCountByUserID5",
				                  "SELECT Count(*) FROM DP_UserBlackList WHERE UserID=?", 100).child(
				                  e("SQL.Method", "Select", "Execute[34796272]")).child(
				                  e("SQL.Database", "jdbc:mysql://10.1.1.63:3306/DianPingCOMM6?characterEncoding=UTF8",
				                        "Execute[34796272]")))
				      .child(
				            t("SQL", "UserBlackList.findCountByUserID6",
				                  "SELECT Count(*) FROM DP_UserBlackList WHERE UserID=?", 100).child(
				                  e("SQL.Method", "Select", "Execute[34796272]")).child(
				                  e("SQL.Database", "jdbc:mysql://10.1.1.63:3306/DianPingCOMM7?characterEncoding=UTF8",
				                        "Execute[34796272]")));

				return t;
			}
		}.build();

		MessageTree tree = new DefaultMessageTree();
		tree.setDomain("cat");
		tree.setHostName("test");
		tree.setIpAddress("test");
		tree.setThreadGroupName("test");
		tree.setThreadId("test");
		tree.setThreadName("test");
		tree.setMessage(message);
		return tree;
	}

}
