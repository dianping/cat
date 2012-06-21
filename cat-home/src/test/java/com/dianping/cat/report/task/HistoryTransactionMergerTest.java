package com.dianping.cat.report.task;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;

public class HistoryTransactionMergerTest {

	@Test
	public void testMerge() throws Exception{
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("transaction.xml"), "utf-8");
		TransactionReport report1 = new DefaultDomParser().parse(oldXml);
		TransactionReport report2 = new DefaultDomParser().parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("result.xml"),
		      "utf-8");
		TransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(report1.getDomain()));

		merger.mergesFrom(report1);
		merger.mergesFrom(report2);

		String actual = new DefaultXmlBuilder().buildXml(report1);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));

	}
}
