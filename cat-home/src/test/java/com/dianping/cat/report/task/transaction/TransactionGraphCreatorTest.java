package com.dianping.cat.report.task.transaction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.page.transaction.Handler.DetailOrder;
import com.dianping.cat.report.page.transaction.Handler.SummaryOrder;
import com.dianping.cat.report.page.transaction.task.TransactionGraphCreator;

public class TransactionGraphCreatorTest {

	@Test
	public void testSplitReportToGraphs() throws Exception {
		TransactionGraphCreator creator = new TransactionGraphCreator();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseTransactionReportForGraph.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(xml);
		List<Graph> graphs = creator.splitReportToGraphs(report.getStartTime(), report.getDomain(), "transaction", report);
		Map<String, Range> realResult = new HashMap<String, Range>();
		Map<String, Range> excepectedResult = buildExcepetedResult();
		buildRealResult(graphs, realResult);

		Assert.assertEquals(excepectedResult.size(),realResult.size());
		for (String str : realResult.keySet()) {
			Range realRange = realResult.get(str);
			Range exceptedRange = excepectedResult.get(str);

			assertStr(realRange.total, exceptedRange.total);
			assertStr(realRange.fail, exceptedRange.fail);
			assertStr(realRange.sum, exceptedRange.sum);
		}
	}

	private void assertStr(String expected, String real) {
		String[] expecteds = expected.split(",");
		String[] reals = real.split(",");

		Assert.assertEquals(expecteds.length, reals.length);
		for (int i = 0; i < expecteds.length; i++) {
			Assert.assertEquals(Double.parseDouble(expecteds[i]), Double.parseDouble(reals[i]));
		}
	}

	private Map<String, Range> buildExcepetedResult() throws Exception {
		Map<String, Range> result = new HashMap<String, Range>();
		String contents = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionGraphResult"), "utf-8");
		String[] lines = contents.split("\n");

		for (String line : lines) {
			String[] tabs = line.split("\t");
			if (tabs.length > 3) {
				Range range = new Range();
				range.total = tabs[1];
				range.sum = tabs[2];
				range.fail = tabs[3];
				result.put(tabs[0], range);
			}
		}
		return result;
	}

	private void buildRealResult(List<Graph> graphs, Map<String, Range> realResult) {
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
				range.sum = records[SummaryOrder.SUM.ordinal()];
				String key = ip + ':' + type;
				
				realResult.put(key, range);
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
				range.sum = records[DetailOrder.SUM.ordinal()];
				realResult.put(ip + ':' + type + ':' + name, range);
			}
		}
	}

	private static class Range {
		public String total;

		public String fail;

		public String sum;
	}
}
