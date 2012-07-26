package com.dianping.cat.report.task;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;
import com.dianping.cat.report.task.transaction.HistoryTransactionReportMerger;

public class HistoryTransactionMergerTest {

	@Test
	public void testMerge() throws Exception{
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("transaction.xml"), "utf-8");
		TransactionReport report1 = new DefaultDomParser().parse(oldXml);
		TransactionReport report2 = new DefaultDomParser().parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("transactionResult.xml"),
		      "utf-8");
		TransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(report1.getDomain()));

		report1.accept(merger);
		report2.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));

	}
}
