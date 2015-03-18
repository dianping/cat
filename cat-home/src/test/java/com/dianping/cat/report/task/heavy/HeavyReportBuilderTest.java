package com.dianping.cat.report.task.heavy;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.transform.DefaultSaxParser;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportMerger;

public class HeavyReportBuilderTest extends ComponentTestCase {

	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("new.xml"), "utf-8");
		HeavyReport reportOld = DefaultSaxParser.parse(oldXml);
		HeavyReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("result.xml"), "utf-8");
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getHeavyReport().toString()
		      .replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));

	}
}
