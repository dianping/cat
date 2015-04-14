package com.dianping.cat.report.task.utilization;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultSaxParser;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportMerger;

public class UtilizationReportMergerTest {
	@Test
	public void testUtilizationReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("UtilizationReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("UtilizationReportNew.xml"), "utf-8");
		UtilizationReport reportOld = DefaultSaxParser.parse(oldXml);
		UtilizationReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("UtilizationReportResult.xml"),
		      "utf-8");
		UtilizationReportMerger merger = new UtilizationReportMerger(new UtilizationReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getUtilizationReport()
		      .toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}

}
