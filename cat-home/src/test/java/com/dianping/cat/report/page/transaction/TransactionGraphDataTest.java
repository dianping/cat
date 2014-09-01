package com.dianping.cat.report.page.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.core.dal.Graph;

public class TransactionGraphDataTest {

	public static final long ONE_HOUR = 3600 * 1000L;

	public SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");

	public void assertArray(double expected, double[] real) {
		for (int i = 0; i < real.length; i++) {
			Assert.assertEquals(expected, real[i]);
		}
	}

	@Test
	public void testBuildGraphDatesByType() {
		HistoryGraphs handler = new HistoryGraphs();
		try {
			Date start = sf.parse("20120601");
			Date end = sf.parse("20120607");
			List<Graph> graphs = buildGraph(start, end);
			String type = "URL";
			Map<String, double[]> graphDates = handler.buildGraphDatasForHour(start, end, type, null, graphs);
			double[] expectTotalCount = graphDates.get("total_count");
			double[] expectFailureCount = graphDates.get("failure_count");
			double[] expectSum = graphDates.get("sum");

			assertArray(12, expectTotalCount);
			assertArray(0, expectFailureCount);
			assertArray(200, expectSum);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBuildGraphDatesByTypeAndName() {
		HistoryGraphs handler = new HistoryGraphs();
		try {
			Date start = sf.parse("20120606");
			Date end = sf.parse("20120606");
			List<Graph> graphs = buildGraph(start, end);
			String type = "Result";
			String name = "cacheService:cacheConfigService_1.0.0:getKeyConfigurations";
			Map<String, double[]> graphDates = handler.buildGraphDatasForHour(start, end, type, name, graphs);
			double[] expectTotalCount = graphDates.get("total_count");
			double[] expectFailureCount = graphDates.get("failure_count");
			double[] expectSum = graphDates.get("sum");

			assertArray(6, expectTotalCount);
			assertArray(0, expectFailureCount);
			assertArray(311.0, expectSum);
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testBuildGraphDatesEmpty() {
		HistoryGraphs handler = new HistoryGraphs();
		try {
			Date start = sf.parse("20120606");
			Date end = sf.parse("20120607");
			List<Graph> graphs = buildGraph(start, end);
			String type = "Result";
			String name = "Result";
			Map<String, double[]> graphDates = handler.buildGraphDatasForHour(start, end, type, name, graphs);
			double[] sum = graphDates.get("sum");
			assertArray(0, sum);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private List<Graph> buildGraph(Date start, Date end) {
		List<Graph> graphs = new ArrayList<Graph>();
		String summary = "Call	13704	0	0.993	505.822	133680.3	7678055.5" + "\n"
		      + "Result	13704	0	0.096	73.104	13377.4	156669.3" + "\n" + "Task	26	0	36.406555	60.022	1102.4	47641.6"
		      + "\n" + "URL	60,60,60,60,60,60,60,60,60,60,60,60	0	11.836555	225244.724	1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000,1000	5.07448582695E10" + "\n";
		String detail = "Call	cacheService:cacheConfigService_1.0.0:getCacheConfigurations	6	0	261.046	481.68	2369.7	989806.8"
		      + "\n"
		      + "Call	cacheService:cacheConfigService_1.0.0:getKeyConfigurations	6	0	40.301	43.161	250.6	10472.6"
		      + "\n"
		      + "Call	shopService:shopService_1.0.0:findShopRegions	2348	0	1.874	505.822	31048.6	1893017.0"
		      + "\n"
		      + "Call	shopService:shopService_1.0.0:findShops	11344	0	0.993	294.569	100011.4	4784759.1"
		      + "\n"
		      + "Result	cacheService:cacheConfigService_1.0.0:getCacheConfigurations	6	0	28.769	64.718	302.0	16596.8"
		      + "\n" + "Result	cacheService:cacheConfigService_1.0.0:getKeyConfigurations	30,30,30,30,30,30,30,30,30,30,30,30	0	6.83	7.583	43.2	311.0";

		for (long i = start.getTime(); i < end.getTime(); i = i + ONE_HOUR) {
			Graph graph = new Graph();
			graph.setDetailContent(detail);
			graph.setSummaryContent(summary);
			graphs.add(graph);
			graph.setPeriod(new Date(i));
		}
		return graphs;
	}
}
