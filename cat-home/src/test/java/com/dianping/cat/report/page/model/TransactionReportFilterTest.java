package com.dianping.cat.report.page.model;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.Handler.TransactionReportFilter;

public class TransactionReportFilterTest {
	@Test
	public void test() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(source);

		TransactionReportFilter f1 = new TransactionReportFilter(null, null, "10.1.77.193");
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction-type.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\r", ""), f1.buildXml(report).replaceAll("\r", ""));

		TransactionReportFilter f2 = new TransactionReportFilter("URL", null, null);
		String expected2 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction-name.xml"), "utf-8");

		Assert.assertEquals(expected2.replaceAll("\\s*", ""), f2.buildXml(report).replaceAll("\\s*", ""));
	}
}
