package com.dianping.cat.consumer.cross;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;

public class CrossReportMergerTest {
	@Test
	public void testCrossReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("cross_analyzer_old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("cross_analyzer_old.xml"), "utf-8");
		CrossReport reportOld = DefaultSaxParser.parse(oldXml);
		CrossReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("cross_analyzer_merger.xml"), "utf-8");
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getCrossReport().toString()
		      .replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
