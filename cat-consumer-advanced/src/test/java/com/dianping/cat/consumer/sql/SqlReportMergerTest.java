package com.dianping.cat.consumer.sql;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;


public class SqlReportMergerTest {
	@Test
	public void testSqlReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("sql_analyzer.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("sql_analyzer.xml"), "utf-8");
		SqlReport reportOld = DefaultSaxParser.parse(oldXml);
		SqlReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("sql_analyzer_merger.xml"), "utf-8");
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getSqlReport().toString()
		      .replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
