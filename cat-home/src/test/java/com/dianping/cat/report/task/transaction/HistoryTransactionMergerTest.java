package com.dianping.cat.report.task.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.transaction.task.HistoryTransactionReportMerger;

public class HistoryTransactionMergerTest {

	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryTransaction.xml"), "utf-8");
		TransactionReport report1 = DefaultSaxParser.parse(oldXml);
		TransactionReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryTransactionMergeResult.xml"),
		      "utf-8");
		HistoryTransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(report1.getDomain()))
		      .setDuration(2);

		report1.accept(merger);
		report2.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));

	}
}
