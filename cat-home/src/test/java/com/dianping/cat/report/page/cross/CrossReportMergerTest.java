package com.dianping.cat.report.page.cross;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;

public class CrossReportMergerTest {
	@Test
	public void testCrossReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("CrossReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("CrossReportNew.xml"), "utf-8");
		CrossReport reportOld = DefaultSaxParser.parse(oldXml);
		CrossReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("CrossReportMergeResult.xml"), "utf-8");
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);
		// Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""),
		// merger.getCrossReport().toString().replaceAll("\r", ""));

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), merger.getCrossReport()
		      .toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""),
		      reportNew.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""),
		      reportOld.toString().replaceAll("\\s*", ""));
	}
}
