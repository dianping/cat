package com.dianping.cat.report.page.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;

public class TransactionReportMergerTest {
	@Test
	public void testTransactionReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
		TransactionReport reportOld = new DefaultDomParser().parse(oldXml);
		TransactionReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportMergeResult.xml"),
		      "utf-8");
		TransactionReportMerger merger = new TransactionReportMerger(reportOld);

		merger.mergesFrom(reportNew);

		String actual = new DefaultXmlBuilder().buildXml(reportOld);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
	
	@Test
	public void testMergeAllIp()throws Exception{
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
		TransactionReport reportOld = new DefaultDomParser().parse(oldXml);
		TransactionReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportMergeAllResult.xml"),
		      "utf-8");
		
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));
		
		merger.setAllIp(true);
		
		reportOld.accept(merger);
		reportNew.accept(merger);
		
		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	
	}
	
	@Test
	public void testMergeAllIpAllName()throws Exception{
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
		TransactionReport reportOld = new DefaultDomParser().parse(oldXml);
		TransactionReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportMergeAllIpAllName.xml"),
		      "utf-8");
		
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));
		
		merger.setAllIp(true);
		merger.setIp(CatString.ALL_IP);
		merger.setAllName(true);
		merger.setType("URL");
		
		reportOld.accept(merger);
		reportNew.accept(merger);
		
		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	
	}
}
