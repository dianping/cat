package com.dianping.cat.consumer.problem;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemReportAllBuilderTest {

	@Test
	public void test() throws Exception{
		ProblemReport report = new ProblemReport("All");
		ProblemReportAllBuilder builder = new ProblemReportAllBuilder(report);
		
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report-builder1.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report-builder2.xml"), "utf-8");
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report-builder-all.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		
		builder.visitProblemReport(reportOld);
		builder.visitProblemReport(reportNew);
		
		Assert.assertEquals(expected.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));
	}
}
