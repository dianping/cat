package com.dianping.cat.report.tool;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.failure.model.transform.DefaultXmlParser;

public class FailureReportToolTest {
	@Test
	public void testFailureReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("FailureReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("FailureReportNew.xml"), "utf-8");
		FailureReport reportOld = new DefaultXmlParser().parse(oldXml);
		FailureReport reportNew = new DefaultXmlParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("FailureReportMergeResult.xml"), "utf-8");

		ReportUtils.mergeFailureReport(reportOld, reportNew);

		String actual = new DefaultXmlBuilder().buildXml(reportOld);

		Assert.assertEquals("Chech the merage result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
}
