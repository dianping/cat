package com.dianping.cat.report.page.transaction;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.transaction.service.LocalTransactionService.TransactionReportFilter;

public class TransactionReportFilterTest {
	@Test
	public void test() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_filter.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(source);
		TransactionReportFilter f1 = new TransactionReportFilter(null, null, "10.1.77.193", 0, 59);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_filter_type.xml"), "utf-8");
		Assert.assertEquals(expected1.replaceAll("\r", ""), f1.buildXml(report).replaceAll("\r", ""));

		TransactionReportFilter f2 = new TransactionReportFilter("URL", null, null, 0, 59);
		String expected2 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_filter_name.xml"), "utf-8");
		Assert.assertEquals(expected2.replaceAll("\r", ""), f2.buildXml(report).replaceAll("\r", ""));
	}
}
