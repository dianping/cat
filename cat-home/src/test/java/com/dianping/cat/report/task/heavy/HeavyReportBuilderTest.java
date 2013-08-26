package com.dianping.cat.report.task.heavy;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.webres.helper.Files;

import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.transform.DefaultSaxParser;

public class HeavyReportBuilderTest extends ComponentTestCase {

	@Test
	public void test() throws Exception{
		HeavyReportBuilder builder = lookup(HeavyReportBuilder.class);
		
		builder.buildHourlyTask("heavy", "cat", new SimpleDateFormat("yyyyMMddHH").parse("2013082609"));
	}
	
	@Test
	public void testMerge()throws Exception{
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("new.xml"), "utf-8");
		HeavyReport reportOld = DefaultSaxParser.parse(oldXml);
		HeavyReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("result.xml"),
		      "utf-8");
		System.out.println(expected);
		HeavyReportMerger merger = new HeavyReportMerger(new HeavyReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getHeavyReport()
		      .toString().replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));

		
	}
}
