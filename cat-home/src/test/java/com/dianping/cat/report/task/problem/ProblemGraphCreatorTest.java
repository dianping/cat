package com.dianping.cat.report.task.problem;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.core.dal.Graph;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemGraphCreatorTest {

	@Test
	public void test() throws Exception {
		ProblemGraphCreator creator = new ProblemGraphCreator();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("problemCreator.xml"), "utf-8");
		ProblemReport report = DefaultSaxParser.parse(xml);
		String summary = Files.forIO().readFrom(getClass().getResourceAsStream("problemGraphSummary"), "utf-8");
		String detail = Files.forIO().readFrom(getClass().getResourceAsStream("problemGraphDetail"), "utf-8");
		List<Graph> graphs = creator.splitReportToGraphs(new Date(), "TuanGouApi", "problem", report);

		Assert.assertEquals(2, graphs.size());
		for (Graph graph : graphs) {
			Assert.assertEquals(summary.replaceAll("\\s*", ""), graph.getSummaryContent().replaceAll("\\s*", ""));
			Assert.assertEquals(detail.replaceAll("\\s*", ""), graph.getDetailContent().replaceAll("\\s*", ""));
		}
	}
}
