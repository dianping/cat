package com.dianping.cat.report.task.problem;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.problem.task.ProblemReportHourlyGraphCreator;

public class ProblemReportHourlyGraphCreatorTest {

	@Test
	public void testGraph() throws Exception {
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("problemModel.xml"), "utf-8");
		ProblemReport report1 = DefaultSaxParser.parse(newXml);
		ProblemReport report2 = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportHourlyGraphResult.xml"),
		      "utf-8");

		ProblemReport result = new ProblemReport(report1.getDomain());

		ProblemReportHourlyGraphCreator creator = new ProblemReportHourlyGraphCreator(result, 10);

		creator.createGraph(report1);
		creator.createGraph(report2);

		String actual = new DefaultXmlBuilder().buildXml(result);
		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
}
