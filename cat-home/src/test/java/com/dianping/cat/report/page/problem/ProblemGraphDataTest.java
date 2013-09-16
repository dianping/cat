package com.dianping.cat.report.page.problem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.core.dal.Graph;

@RunWith(JUnit4.class)
public class ProblemGraphDataTest {

	public static final long ONE_HOUR = 3600 * 1000L;

	public static final long ONE_DAY = 24 * ONE_HOUR;

	@Test
	public void testBuildGraphDatasByType() throws IOException {
		HistoryGraphs handler = new HistoryGraphs();
		long time = System.currentTimeMillis();
		time = time - time % (ONE_DAY);
		Date start = new Date(time - ONE_DAY);
		Date end = new Date(time);
		List<Graph> graphs = new ArrayList<Graph>();

		time = start.getTime();
		for (; time < end.getTime(); time += 3600 * 1000) {
			Date addtime = new Date(time);
			graphs.add(creatGraph(addtime));
		}
		Map<String, double[]> graphDatas = handler.buildGraphDatasFromHour(start, end, HeartbeatAnalyzer.ID, "", graphs);
		double[] errors = graphDatas.get("errors");
		double[] expectErrors = new double[errors.length];
		for (int i = 0; i < expectErrors.length; i++) {
			expectErrors[i] = 1;
		}
		Assert.assertEquals(true, Arrays.equals(errors, expectErrors));
	}

	@Test
	public void testBuildGraphDatasByTypeAndName() throws IOException {
		HistoryGraphs handler = new HistoryGraphs();
		long time = System.currentTimeMillis();
		time = time - time % (ONE_DAY);
		Date start = new Date(time - ONE_DAY);
		Date end = new Date(time);

		List<Graph> graphs = new ArrayList<Graph>();
		time = start.getTime();
		for (; time < end.getTime(); time += 3600 * 1000) {
			Date addtime = new Date(time);
			graphs.add(creatGraph(addtime));
		}
		Map<String, double[]> graphDatas = handler.buildGraphDatasFromHour(start, end, "long-url", "/addcheckin.bin", graphs);
		double[] errors = graphDatas.get("errors");
		double[] expectErrors = new double[errors.length];
		for (int i = 2; i < expectErrors.length; i = i + 60) {
			expectErrors[i] = 1;
		}
		Assert.assertEquals(true, Arrays.equals(errors, expectErrors));
	}

	private String getContent(String fileName) throws IOException {
		String s = Files.forIO().readFrom(getClass().getResourceAsStream(fileName), "utf-8");
		return s;
	}

	private Graph creatGraph(Date period) throws IOException {
		Graph graph = new Graph();
		graph.setPeriod(period);
		String detail = getContent("detail");
		graph.setDetailContent(detail);
		graph.setSummaryContent(getContent("summary"));
		return graph;
	}
}
