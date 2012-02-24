package com.dianping.cat.report.tool;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;

public class TransactionReportToolTest {

	@Test
	public void testTransactionReportMerge() throws Exception{
		String oldXml = Files.forIO().readFrom(TransactionReportToolTest.class.getResourceAsStream("TransactionReportOld.xml"),"utf-8");
		String newXml = Files.forIO().readFrom(TransactionReportToolTest.class.getResourceAsStream("TransactionReportNew.xml"),"utf-8");
		TransactionReport reportOld = new DefaultXmlParser().parse(oldXml);
		TransactionReport reportNew = new DefaultXmlParser().parse(newXml);
		String result = Files.forIO().readFrom(TransactionReportToolTest.class.getResourceAsStream("TransactionReportMergeResult.xml"),"utf-8");
		ReportUtils.mergeTransactionReport(reportOld, reportNew);
		
		result=result.replaceAll("\\s","");
		String buildXml = new DefaultXmlBuilder().buildXml(reportOld).replaceAll("\\s", "");
		Assert.assertEquals("Chech the merage result!",result,buildXml);
	}
}

