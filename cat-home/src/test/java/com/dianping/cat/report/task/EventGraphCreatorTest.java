package com.dianping.cat.report.task;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.report.page.event.Handler.DetailOrder;
import com.dianping.cat.report.page.event.Handler.SummaryOrder;
import com.dianping.cat.report.task.event.EventGraphCreator;

public class EventGraphCreatorTest {

	@Test
	public void test() throws Exception {
		EventGraphCreator creator = new EventGraphCreator();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("eventCreator.xml"), "utf-8");
		EventReport report = DefaultSaxParser.parse(xml);
		Date date = new Date();
		List<Graph> graphs = creator.splitReportToGraphs(date, "MobileApi", "event", report);
		Map<String, Range> result = new HashMap<String, Range>();
		Map<String, Range> result2 = new HashMap<String, Range>();
		for (Graph graph : graphs) {
			String ip = graph.getIp();
			String summaryContent = graph.getSummaryContent();
			String lines[] = summaryContent.split("\n");
			for (String line : lines) {
				String records[] = line.split("\t");
				String type = records[0];
				Range range = new Range();
				range.total = getSum(records[SummaryOrder.TOTAL_COUNT.ordinal()]);
				range.fail = getSum(records[SummaryOrder.FAILURE_COUNT.ordinal()]);
				result.put(ip + ',' + type, range);
			}
			String detailContent = graph.getDetailContent();
			lines = detailContent.split("\n");
			for (String line : lines) {
				String records[] = line.split("\t");
				String type = records[0];
				String name = records[1];
				Range range = new Range();
				range.total = getSum(records[DetailOrder.TOTAL_COUNT.ordinal()]);
				range.fail = getSum(records[DetailOrder.FAILURE_COUNT.ordinal()]);
				result.put(ip + ',' + type + ',' + name, range);
			}
		}

		EventHelpGraphCreator creator2 = new EventHelpGraphCreator();
		List<Graph> graphs2 = creator2.splitReportToGraphs(date, "MobileApi", "event", report);
		for (Graph graph : graphs2) {
			String ip = graph.getIp();
			String summaryContent = graph.getSummaryContent();
			String lines[] = summaryContent.split("\n");
			for (String line : lines) {
				String records[] = line.split("\t");
				String type = records[0];
				Range range = new Range();
				range.total = getSum(records[SummaryOrder.TOTAL_COUNT.ordinal()]);
				range.fail = getSum(records[SummaryOrder.FAILURE_COUNT.ordinal()]);
				result2.put(ip + ',' + type, range);
			}
			String detailContent = graph.getDetailContent();
			lines = detailContent.split("\n");
			for (String line : lines) {
				String records[] = line.split("\t");
				String type = records[0];
				String name = records[1];
				Range range = new Range();
				range.total = getSum(records[DetailOrder.TOTAL_COUNT.ordinal()]);
				range.fail = getSum(records[DetailOrder.FAILURE_COUNT.ordinal()]);
				result2.put(ip + ',' + type + ',' + name, range);
			}
		}

		Assert.assertEquals(result.size(), result2.size());
		for (String str : result.keySet()) {
			Range range1 = result.get(str);
			Range range2 = result2.get(str);
			Assert.assertEquals(true, range1.fail - range2.fail < 0.1);
			Assert.assertEquals(true, range1.total - range2.total < 0.1);
		}
	}

	private double getSum(String str) {
		String[] strs = str.split(",");
		double sum = 0;
		for (String temp : strs) {
			sum = sum + Double.parseDouble(temp);
		}
		return sum;
	}

	static class Range {
		double total;

		double fail;
	}
}
