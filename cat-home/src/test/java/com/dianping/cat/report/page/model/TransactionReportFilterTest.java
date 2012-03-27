package com.dianping.cat.report.page.model;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.report.page.model.Handler.TransactionReportFilter;
import com.site.helper.Files;

public class TransactionReportFilterTest {
	@Test
	public void test() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction.xml"), "utf-8");
		TransactionReport report = parser.parse(source);

		TransactionReportFilter f1 = new TransactionReportFilter(null, null);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction-type.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\r", ""), f1.buildXml(report).replaceAll("\r", ""));
		
		TransactionReportFilter f2 = new TransactionReportFilter("URL", null);
		String expected2 = Files.forIO().readFrom(getClass().getResourceAsStream("transaction-name.xml"), "utf-8");
		
		Assert.assertEquals(expected2.replaceAll("\r", ""), f2.buildXml(report).replaceAll("\r", ""));
	}
}
