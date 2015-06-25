package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class TransactionReportFilterTest {

	private final int MAX_URL_NUM = 401;

	@Test
	public void whether_url_has_max_names() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_report_filter.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(source);

		TransactionType type = report.findMachine("10.1.77.193").findType("URL");

		for (int i = 0; i < 3500; i++) {
			type.addName(new TransactionName("Test" + i));
		}
		TransactionReportCountFilter f1 = new TransactionReportCountFilter();

		f1.visitTransactionReport(report);

		String filterReport = report.toString();
		TransactionReport newReport = DefaultSaxParser.parse(filterReport);

		int newSize = newReport.findMachine("10.1.77.193").findType("URL").getNames().size();

		Assert.assertEquals(MAX_URL_NUM, newSize);
	}
}
