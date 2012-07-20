package com.dianping.cat.consumer.transaction;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer.TransactionReportFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.site.helper.Files;

public class TransactionReportFilterTest {
	@Test
	public void test() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("transaction.xml"), "utf-8");
		TransactionReport report = parser.parse(source);

		TransactionReportFilter f1 = new TransactionReportFilter();
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("transactionFilter.xml"), "utf-8");
		report.accept(f1);
		
		Assert.assertEquals(expected1.replaceAll("\\s", ""), report.toString().replaceAll("\\s", ""));
	}
}
