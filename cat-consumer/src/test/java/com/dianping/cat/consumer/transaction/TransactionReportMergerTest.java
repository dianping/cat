package com.dianping.cat.consumer.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class TransactionReportMergerTest {
	@Test
	public void testTransactionReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_report_old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_report_new.xml"), "utf-8");
		TransactionReport reportOld = DefaultSaxParser.parse(oldXml);
		TransactionReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("transaction_report_mergeResult.xml"),
		      "utf-8");
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getTransactionReport()
		      .toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
