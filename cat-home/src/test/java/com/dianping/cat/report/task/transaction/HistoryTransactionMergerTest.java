package com.dianping.cat.report.task.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;

public class HistoryTransactionMergerTest {

	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryTransaction.xml"), "utf-8");
		TransactionReport report1 = DefaultSaxParser.parse(oldXml);
		TransactionReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryTransactionMergeResult.xml"),
		      "utf-8");
		TransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(report1.getDomain()));

		report1.accept(merger);
		report2.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));

	}
}
