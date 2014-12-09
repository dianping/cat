package com.dianping.cat.consumer.problem;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemFilterTest {

	@Test
	public void test() throws Exception {
		ProblemReportFilter problemReportURLFilter = new ProblemReportFilter(5);
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("problemURLFilter.xml"), "utf-8");
		String resultXml = Files.forIO().readFrom(getClass().getResourceAsStream("problemURLFilterResult.xml"), "utf-8");
		ProblemReport report = DefaultSaxParser.parse(oldXml);
		problemReportURLFilter.visitProblemReport(report);

		Assert.assertEquals(resultXml.replaceAll("\r", ""), report.toString().replaceAll("\r", ""));

	}
}
