package com.dianping.cat.report.page.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;

public class TransactionReportMergerTest {
	@Test
	public void testTransactionReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
		TransactionReport reportOld = new DefaultXmlParser().parse(oldXml);
		TransactionReport reportNew = new DefaultXmlParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportMergeResult.xml"),
		      "utf-8");
		TransactionReportMerger merger = new TransactionReportMerger(reportOld);

		merger.mergesFrom(reportNew);

		String actual = new DefaultXmlBuilder().buildXml(reportOld);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
}
