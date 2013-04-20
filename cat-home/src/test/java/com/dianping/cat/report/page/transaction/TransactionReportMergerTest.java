package com.dianping.cat.report.page.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;

public class TransactionReportMergerTest {
	@Test
	public void testTransactionReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
		TransactionReport reportOld = DefaultSaxParser.parse(oldXml);
		TransactionReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportMergeResult.xml"),
		      "utf-8");
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getTransactionReport()
		      .toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}

	@Test
	public void testMergeAllIp() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
		TransactionReport reportOld = DefaultSaxParser.parse(oldXml);
		TransactionReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportMergeAllResult.xml"),
		      "utf-8");

		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));

		merger.setAllIp(true);

		reportOld.accept(merger);
		reportNew.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replace("\r", ""), reportOld.toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}

	@Test
	public void testMergeAllIpAllName() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
		TransactionReport reportOld = DefaultSaxParser.parse(oldXml);
		TransactionReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(
		      getClass().getResourceAsStream("TransactionReportMergeAllIpAllName.xml"), "utf-8");

		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));

		merger.setAllIp(true);
		merger.setIp(CatString.ALL_IP);
		merger.setAllName(true);
		merger.setType("URL");

		reportOld.accept(merger);
		reportNew.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replace("\r", ""), reportOld.toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
