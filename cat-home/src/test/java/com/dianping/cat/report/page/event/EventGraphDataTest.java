package com.dianping.cat.report.page.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.helper.Files;

import com.dianping.cat.core.dal.Graph;

@RunWith(JUnit4.class)
public class EventGraphDataTest {

	@Test
	public void testBuildGraphDatasByType() throws IOException {

		HistoryGraphs handler = new HistoryGraphs();
		long time = System.currentTimeMillis();
		time = time - time % (3600 * 1000 * 24);
		Date start = new Date(time - 3600 * 1000 * 24);
		Date end = new Date(time);

		List<Graph> graphs = new ArrayList<Graph>();
		time = start.getTime();
		for (; time < end.getTime(); time += 3600 * 1000) {
			Date addtime = new Date(time);
			graphs.add(creatGraph(addtime));
		}
		Map<String, double[]> graphDatas = handler.buildGraphDatasForHour(start, end, "URL", "", graphs);
		double[] total_count = graphDatas.get("total_count");
		double[] failure_count = graphDatas.get("failure_count");
		
		assertArray(6, total_count);
		assertArray(0, failure_count);
	}

	@Test
	public void testBuildGraphDatasByTypeAndName() throws IOException {
		HistoryGraphs handler = new HistoryGraphs();
		long time = System.currentTimeMillis();
		time = time - time % (3600 * 1000 * 24);
		Date start = new Date(time - 3600 * 1000 * 24);
		Date end = new Date(time);

		List<Graph> graphs = new ArrayList<Graph>();
		time = start.getTime();
		for (; time < end.getTime(); time += 3600 * 1000) {
			Date addtime = new Date(time);
			graphs.add(creatGraph(addtime));
		}
		Map<String, double[]> graphDatas = handler.buildGraphDatasForHour(start, end, "URL", "ClientInfo", graphs);
		double[] total_count = graphDatas.get("total_count");
		double[] failure_count = graphDatas.get("failure_count");
		assertArray(3, total_count);
		assertArray(0, failure_count);
	}

	private String getContent(String fileName) throws IOException {
		return Files.forIO().readFrom(getClass().getResourceAsStream(fileName), "utf-8");
	}

	private Graph creatGraph(Date period) throws IOException {
		Graph graph = new Graph();
		graph.setPeriod(period);
		graph.setDetailContent(getContent("detail"));
		graph.setSummaryContent(getContent("summary"));
		return graph;
	}

	public void assertArray(double expected, double[] real) {
		for (int i = 0; i < real.length; i++) {
			Assert.assertEquals(expected, real[i]);
		}
	}
}
