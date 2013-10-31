package com.dianping.cat.consumer.problem;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemReportMergerTest {
	@Test
	public void testProblemReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportNew.xml"), "utf-8");
		ProblemReport reportOld = DefaultSaxParser.parse(oldXml);
		ProblemReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportMergeResult.xml"), "utf-8");
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getProblemReport().toString()
		      .replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
