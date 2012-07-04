package com.dianping.cat.report.task;

import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultDomParser;
import com.dianping.cat.hadoop.dal.Graph;

public class ProblemGraphCreatorTest {

	@Test
	public void test() throws Exception{
		ProblemGraphCreator creator = new ProblemGraphCreator();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("problemCreator.xml"), "utf-8");
		ProblemReport report = new DefaultDomParser().parse(xml);
		
		String summary = Files.forIO().readFrom(getClass().getResourceAsStream("problemGraphSummary"), "utf-8");
		
		String detail = Files.forIO().readFrom(getClass().getResourceAsStream("problemGraphDetail"), "utf-8");
		
		List<Graph> graphs = creator.splitReportToGraphs(new Date(), "TuanGouApi", "problem", report);
		
		Assert.assertEquals(2, graphs.size());
		for(Graph graph:graphs){
			Assert.assertEquals(summary, graph.getSummaryContent());
			Assert.assertEquals(detail, graph.getDetailContent());
		}
	}
}
