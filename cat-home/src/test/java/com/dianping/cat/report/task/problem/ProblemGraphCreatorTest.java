package com.dianping.cat.report.task.problem;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.page.problem.task.ProblemGraphCreator;

public class ProblemGraphCreatorTest {

	@Test
	public void test() throws Exception {
		ProblemGraphCreator creator = new ProblemGraphCreator();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("problemCreator.xml"), "utf-8");
		ProblemReport report = DefaultSaxParser.parse(xml);
		String summary = Files.forIO().readFrom(getClass().getResourceAsStream("problemGraphSummary"), "utf-8");
		String detail = Files.forIO().readFrom(getClass().getResourceAsStream("problemGraphDetail"), "utf-8");
		List<Graph> graphs = creator.splitReportToGraphs(report.getStartTime(), report.getDomain(), "problem", report);

		Assert.assertEquals(2, graphs.size());
		for (Graph graph : graphs) {
			Assert.assertEquals(summary.replaceAll("\r", ""), graph.getSummaryContent().replaceAll("\r", ""));
			Assert.assertEquals(detail.replaceAll("\r", ""), graph.getDetailContent().replaceAll("\r", ""));
		}
	}
}
