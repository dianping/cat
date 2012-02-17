package com.dianping.cat.report.tool;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.failure.model.transform.DefaultXmlParser;

public class FailureReportToolTest {

	@Test
	public void testFailureReportMerge() throws Exception{
		String oldXml = Files.forIO().readFrom(FailureReportToolTest.class.getResourceAsStream("FailureReportOld.xml"),"utf-8");
		String newXml = Files.forIO().readFrom(FailureReportToolTest.class.getResourceAsStream("FailureReportNew.xml"),"utf-8");
		FailureReport reportOld = new DefaultXmlParser().parse(oldXml);
		FailureReport reportNew = new DefaultXmlParser().parse(newXml);
		String result = Files.forIO().readFrom(FailureReportToolTest.class.getResourceAsStream("FailureReportMergeResult.xml"),"utf-8");
		
		ReportUtils.mergeFailureReport(reportOld, reportNew);
		Assert.assertEquals("Chech the merage result!",result,new DefaultXmlBuilder().buildXml(reportOld));
	}
}

