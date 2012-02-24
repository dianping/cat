package com.dianping.cat.report.tool;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.ip.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.ip.model.transform.DefaultXmlParser;
import com.dianping.cat.consumer.ip.model.entity.IpReport;

public class IpReportToolTest {
	@Test
	public void testIpReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("IpReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("IpReportNew.xml"), "utf-8");
		IpReport reportOld = new DefaultXmlParser().parse(oldXml);
		IpReport reportNew = new DefaultXmlParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("IpReportMergeResult.xml"), "utf-8");

		ReportUtils.mergeIpReport(reportOld, reportNew);

		String actual = new DefaultXmlBuilder().buildXml(reportOld);

		Assert.assertEquals("Chech the merage result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
}
