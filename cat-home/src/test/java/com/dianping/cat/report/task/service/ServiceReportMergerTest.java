package com.dianping.cat.report.task.service;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.service.transform.DefaultSaxParser;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportMerger;


public class ServiceReportMergerTest {
	@Test
	public void testServiceReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ServiceReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ServiceReportNew.xml"), "utf-8");
		ServiceReport reportOld = DefaultSaxParser.parse(oldXml);
		ServiceReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ServiceReportResult.xml"),
		      "utf-8");
		ServiceReportMerger merger = new ServiceReportMerger(new ServiceReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getServiceReport()
		      .toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
