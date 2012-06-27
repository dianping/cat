package com.dianping.cat.report.page.problem;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultDomParser;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;

public class ProblemReportMergerTest {
	@Test
	public void testProblemReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportNew.xml"), "utf-8");
		ProblemReport reportOld = new DefaultDomParser().parse(oldXml);
		ProblemReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportMergeResult.xml"),
		      "utf-8");
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);
		
		
		System.out.println(reportOld);
		System.out.println(reportNew);
		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), reportNew.toString().replace("\r", ""));
		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getProblemReport().toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}

//	@Test
//	public void testMergeAllIp() throws Exception {
//		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
//		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
//		TransactionReport reportOld = new DefaultDomParser().parse(oldXml);
//		TransactionReport reportNew = new DefaultDomParser().parse(newXml);
//		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportMergeAllResult.xml"),
//		      "utf-8");
//
//		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));
//
//		merger.setAllIp(true);
//
//		reportOld.accept(merger);
//		reportNew.accept(merger);
//
//		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());
//
//		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
//		Assert.assertEquals("Source report is changed!", oldXml.replace("\r", ""), reportOld.toString().replace("\r", ""));
//		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
//	}
//
//	@Test
//	public void testMergeAllIpAllName() throws Exception {
//		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportOld.xml"), "utf-8");
//		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReportNew.xml"), "utf-8");
//		TransactionReport reportOld = new DefaultDomParser().parse(oldXml);
//		TransactionReport reportNew = new DefaultDomParser().parse(newXml);
//		String expected = Files.forIO().readFrom(
//		      getClass().getResourceAsStream("TransactionReportMergeAllIpAllName.xml"), "utf-8");
//
//		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportOld.getDomain()));
//
//		merger.setAllIp(true);
//		merger.setIp(CatString.ALL_IP);
//		merger.setAllName(true);
//		merger.setType("URL");
//
//		reportOld.accept(merger);
//		reportNew.accept(merger);
//
//		String actual = new DefaultXmlBuilder().buildXml(merger.getTransactionReport());
//
//		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
//		Assert.assertEquals("Source report is changed!", oldXml.replace("\r", ""), reportOld.toString().replace("\r", ""));
//		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
//	}
}
