package com.dianping.cat.report.page.sql;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.sql.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.model.sql.SqlReportMerger;

public class SqlReportMergerTest {
	@Test
	public void testSqlReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("SqlReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("SqlReportNew.xml"), "utf-8");
		SqlReport reportOld = DefaultSaxParser.parse(oldXml);
		SqlReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("SqlReportMergeResult.xml"), "utf-8");
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		// Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""),
		// merger.getSqlReport().toString().replaceAll("\r", ""));

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), merger.getSqlReport().toString()
		      .replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""),
		      reportNew.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""),
		      reportOld.toString().replaceAll("\\s*", ""));
	}

	@Test
	public void testMergeAllDatabase() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("SqlReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("SqlReportNew.xml"), "utf-8");
		SqlReport reportOld = DefaultSaxParser.parse(oldXml);
		SqlReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("SqlReportMergeAllResult.xml"), "utf-8");

		SqlReportMerger merger = new SqlReportMerger(new SqlReport(reportOld.getDomain()));

		merger.setAllDatabase(true);

		reportOld.accept(merger);
		reportNew.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getSqlReport());

		// Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""), actual.replaceAll("\r", ""));

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), actual.replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""),
		      reportOld.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""),
		      reportNew.toString().replaceAll("\\s*", ""));
	}
}
