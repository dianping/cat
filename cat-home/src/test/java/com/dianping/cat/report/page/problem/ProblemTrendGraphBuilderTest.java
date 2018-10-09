package com.dianping.cat.report.page.problem;

import java.util.Arrays;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.problem.transform.ProblemTrendGraphBuilder;
import com.dianping.cat.report.page.problem.transform.ProblemTrendGraphBuilder.ProblemReportVisitor;

public class ProblemTrendGraphBuilderTest {

	@Test
	public void test() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemReportDailyGraph.xml"), "utf-8");
		ProblemReport report = DefaultSaxParser.parse(xml);

		ProblemReportVisitor visitor = new ProblemTrendGraphBuilder().new ProblemReportVisitor("10.1.1.166", "long-url",
		      "/location.bin");
		visitor.visitProblemReport(report);

		double[] datas = visitor.getDatas();
		double[] expectErrors = new double[datas.length];

		for (int i = 0; i < datas.length; i++) {
			expectErrors[i] = 45;
		}
		Assert.assertEquals(true, Arrays.equals(datas, expectErrors));
	}
}
