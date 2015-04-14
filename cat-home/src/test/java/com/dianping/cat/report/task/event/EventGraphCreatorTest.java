package com.dianping.cat.report.task.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.page.event.Handler.DetailOrder;
import com.dianping.cat.report.page.event.Handler.SummaryOrder;
import com.dianping.cat.report.page.event.task.EventGraphCreator;

public class EventGraphCreatorTest {

	@Test
	public void testSplitReportToGraphs() throws Exception {
		EventGraphCreator creator = new EventGraphCreator();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseEventGraphReport.xml"), "utf-8");
		EventReport report = DefaultSaxParser.parse(xml);
		List<Graph> graphs = creator.splitReportToGraphs(report.getStartTime(), report.getDomain(), "event", report);

		// List<Graph> graphs = creator.buildGraph(report);
		Map<String, Range> realResult = new HashMap<String, Range>();
		Map<String, Range> excepectedResult = buildExceptedResult();
		buildResultResult(graphs, realResult);

		Assert.assertEquals(realResult.size(), excepectedResult.size());
		for (String str : realResult.keySet()) {
			Range range1 = realResult.get(str);
			Range range2 = excepectedResult.get(str);
			
			Assert.assertEquals("key:" + str, range1.total, range2.total);
			Assert.assertEquals("key:" + str, range1.fail, range2.fail);
		}
	}

	private Map<String, Range> buildExceptedResult() throws Exception {
		Map<String, Range> result = new HashMap<String, Range>();
		String contents = Files.forIO().readFrom(getClass().getResourceAsStream("EventGraphResult"), "utf-8");
		String[] lines = contents.split("\n");

		for (String line : lines) {
			String[] tabs = line.split("\t");
			if (tabs.length > 2) {
				Range range = new Range();
				range.total = tabs[1];
				range.fail = tabs[2];
				result.put(tabs[0], range);
			}
		}
		return result;
	}

	private void buildResultResult(List<Graph> graphs, Map<String, Range> realResult) {
		for (Graph graph : graphs) {
			String ip = graph.getIp();
			String summaryContent = graph.getSummaryContent();
			String lines[] = summaryContent.split("\n");
			for (String line : lines) {
				String records[] = line.split("\t");
				String type = records[0];
				Range range = new Range();
				range.total = records[SummaryOrder.TOTAL_COUNT.ordinal()];
				range.fail = records[SummaryOrder.FAILURE_COUNT.ordinal()];
				realResult.put(ip + ':' + type, range);
			}
			String detailContent = graph.getDetailContent();
			lines = detailContent.split("\n");
			for (String line : lines) {
				String records[] = line.split("\t");
				String type = records[0];
				String name = records[1];
				Range range = new Range();
				range.total = records[DetailOrder.TOTAL_COUNT.ordinal()];
				range.fail = records[DetailOrder.FAILURE_COUNT.ordinal()];
				realResult.put(ip + ':' + type + ':' + name, range);
			}
		}
	}

	static class Range {
		String total;

		String fail;
	}
}
