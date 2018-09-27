package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class TransactionReportTypeAggergatorTest {

	@Test
	public void test() throws Exception {
		TransactionReport report1 = parse("transaction_report_aggergator1.xml");
		TransactionReport report2 = parse("transaction_report_aggergator2.xml");
		TransactionReport report3 = parse("transaction_report_aggergator3.xml");
		TransactionReport report4 = parse("transaction_report_aggergator4.xml");
		TransactionReport result = new TransactionReport("All");
		TransactionReportTypeAggregator aggergator = new TransactionReportTypeAggregator(result,
		      new ExtendAllTransactionConfigManager());

		aggergator.visitTransactionReport(report1);
		aggergator.visitTransactionReport(report2);
		aggergator.visitTransactionReport(report3);
		aggergator.visitTransactionReport(report4);

		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_report_aggergatorAll.xml"),
		      "utf-8");
		Assert.assertEquals(expected.replaceAll("\\r", ""), result.toString().replaceAll("\\r", ""));
	}

	private TransactionReport parse(String name) throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream(name), "utf-8");
		return DefaultSaxParser.parse(source);
	}

	public static class ExtendAllTransactionConfigManager extends AllReportConfigManager {

		@Override
		public boolean validate(String report, String type) {
			return true;
		}

		@Override
		public boolean validate(String report, String type, String name) {
			return true;
		}
	}

}
