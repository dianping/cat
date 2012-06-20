package com.dianping.cat.report.page.transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.hadoop.dal.Graph;

public class GraphDateTest {

	public static final long ONE_HOUR = 3600 * 1000L;

	@Test
	public void testBuildGraphDatesByType() {
		Handler handler = new Handler();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHH");
		try {
			Date start = sf.parse("2012060612");
			Date end = sf.parse("2012060622");
			List<Graph> graphs = buildGraph(start, end);
			String type = "URL";
			Map<String, double[]> graphDates = handler.buildGraphDates(start, end, type, null, graphs);
			double[] total_count = { 15, 15, 15, 15, 15, 15, 15, 15, 15, 15 };
			double[] failure_count = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			double[] min = { 11.836555, 11.836555, 11.836555, 11.836555, 11.836555, 11.836555, 11.836555, 11.836555,
			      11.836555, 11.836555 };
			double[] max = { 225244.724, 225244.724, 225244.724, 225244.724, 225244.724, 225244.724, 225244.724,
			      225244.724, 225244.724, 225244.724 };
			double[] sum = { 234038.7, 234038.7, 234038.7, 234038.7, 234038.7, 234038.7, 234038.7, 234038.7, 234038.7,
			      234038.7 };
			double[] sum2 = { 5.07448582695E10, 5.07448582695E10, 5.07448582695E10, 5.07448582695E10, 5.07448582695E10,
			      5.07448582695E10, 5.07448582695E10, 5.07448582695E10, 5.07448582695E10, 5.07448582695E10 };
			double[] expectTotalCount = graphDates.get("total_count");
			double[] expectFailureCount = graphDates.get("failure_count");
			double[] expectMin = graphDates.get("min");
			double[] expectMax = graphDates.get("max");
			double[] expectSum2 = graphDates.get("sum2");
			double[] expectSum = graphDates.get("sum");
			
			Assert.assertEquals(true, Arrays.equals(total_count, expectTotalCount));
			Assert.assertEquals(true, Arrays.equals(failure_count, expectFailureCount));
			Assert.assertEquals(true, Arrays.equals(min, expectMin));
			Assert.assertEquals(true, Arrays.equals(max, expectMax));
			Assert.assertEquals(true, Arrays.equals(sum, expectSum));
			Assert.assertEquals(true, Arrays.equals(sum2, expectSum2));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBuildGraphDatesByTypeAndName() {
		Handler handler = new Handler();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHH");
		try {
			Date start = sf.parse("2012060612");
			Date end = sf.parse("2012060622");
			List<Graph> graphs = buildGraph(start, end);
			String type = "Result";
			String name = "cacheService:cacheConfigService_1.0.0:getKeyConfigurations";
			Map<String, double[]> graphDates = handler.buildGraphDates(start, end, type, name, graphs);
			double[] total_count = { 6, 6, 6, 6, 6, 6, 6, 6, 6, 6 };
			double[] failure_count = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			double[] min = { 6.83, 6.83, 6.83, 6.83, 6.83, 6.83, 6.83, 6.83, 6.83, 6.83 };
			double[] max = { 7.583, 7.583, 7.583, 7.583, 7.583, 7.583, 7.583, 7.583, 7.583, 7.583 };
			double[] sum = { 43.2, 43.2, 43.2, 43.2, 43.2, 43.2, 43.2, 43.2, 43.2, 43.2 };
			double[] sum2 = { 311.0, 311.0, 311.0, 311.0, 311.0, 311.0, 311.0, 311.0, 311.0, 311.0 };
			double[] expectTotalCount = graphDates.get("total_count");
			double[] expectFailureCount = graphDates.get("failure_count");
			double[] expectMin = graphDates.get("min");
			double[] expectMax = graphDates.get("max");
			double[] expectSum2 = graphDates.get("sum2");
			double[] expectSum = graphDates.get("sum");
			
			Assert.assertEquals(true, Arrays.equals(total_count, expectTotalCount));
			Assert.assertEquals(true, Arrays.equals(failure_count, expectFailureCount));
			Assert.assertEquals(true, Arrays.equals(min, expectMin));
			Assert.assertEquals(true, Arrays.equals(max, expectMax));
			Assert.assertEquals(true, Arrays.equals(sum, expectSum));
			Assert.assertEquals(true, Arrays.equals(sum2, expectSum2));
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testBuildGraphDatesEmpty() {
		Handler handler = new Handler();
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHH");
		try {
			Date start = sf.parse("2012060612");
			Date end = sf.parse("2012060622");
			List<Graph> graphs = buildGraph(start, end);
			String type = "Result";
			String name = "Result";
			Map<String, double[]> graphDates = handler.buildGraphDates(start, end, type, name, graphs);
			double[] expectSum = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			double[] sum = graphDates.get("sum");
			
			Assert.assertEquals(true, Arrays.equals(sum, expectSum));
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	private List<Graph> buildGraph(Date start, Date end) {
		List<Graph> graphs = new ArrayList<Graph>();
		String summary = "Call	13704	0	0.993	505.822	133680.3	7678055.5" + "\n"
		      + "Result	13704	0	0.096	73.104	13377.4	156669.3" + "\n" + "Task	26	0	36.406555	60.022	1102.4	47641.6"
		      + "\n" + "URL	15	0	11.836555	225244.724	234038.7	5.07448582695E10" + "\n";
		String detail = "Call	cacheService:cacheConfigService_1.0.0:getCacheConfigurations	6	0	261.046	481.68	2369.7	989806.8"
		      + "\n"
		      + "Call	cacheService:cacheConfigService_1.0.0:getKeyConfigurations	6	0	40.301	43.161	250.6	10472.6"
		      + "\n"
		      + "Call	shopService:shopService_1.0.0:findShopRegions	2348	0	1.874	505.822	31048.6	1893017.0"
		      + "\n"
		      + "Call	shopService:shopService_1.0.0:findShops	11344	0	0.993	294.569	100011.4	4784759.1"
		      + "\n"
		      + "Result	cacheService:cacheConfigService_1.0.0:getCacheConfigurations	6	0	28.769	64.718	302.0	16596.8"
		      + "\n" + "Result	cacheService:cacheConfigService_1.0.0:getKeyConfigurations	6	0	6.83	7.583	43.2	311.0";
		List<Date> periods = new ArrayList<Date>();
		
		for (long i = start.getTime(); i < end.getTime(); i = i + ONE_HOUR) {
			periods.add(new Date(i));
		}
		for (int i = 0; i < 10; i++) {
			Graph graph = new Graph();
			graph.setDetailContent(detail);
			graph.setSummaryContent(summary);
			graphs.add(graph);
			graph.setPeriod(periods.get(i));
		}
		return graphs;
	}
}
