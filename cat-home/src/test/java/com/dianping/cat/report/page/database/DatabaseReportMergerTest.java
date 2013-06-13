package com.dianping.cat.report.page.database;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.database.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.model.database.DatabaseReportMerger;

public class DatabaseReportMergerTest {
	@Test
	public void testDatabaseReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("DatabaseReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("DatabaseReportNew.xml"), "utf-8");
		DatabaseReport reportOld = DefaultSaxParser.parse(oldXml);
		DatabaseReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO()
		      .readFrom(getClass().getResourceAsStream("DatabaseReportMergeResult.xml"), "utf-8");
		DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(reportOld.getDatabase()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		// Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""),
		// merger.getDatabaseReport().toString().replaceAll("\r", ""));

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), merger.getDatabaseReport()
		      .toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""),
		      reportNew.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""),
		      reportOld.toString().replaceAll("\\s*", ""));
	}

	@Test
	public void testMergeAllDomain() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("DatabaseReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("DatabaseReportNew.xml"), "utf-8");
		DatabaseReport reportOld = DefaultSaxParser.parse(oldXml);
		DatabaseReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("DatabaseReportMergeAllResult.xml"),
		      "utf-8");

		DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(reportOld.getDatabase()));

		merger.setAllDomain(true);

		reportOld.accept(merger);
		reportNew.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getDatabaseReport());

		// Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""), actual.replaceAll("\r", ""));

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), actual.replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""),
		      reportOld.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""),
		      reportNew.toString().replaceAll("\\s*", ""));
	}
}
