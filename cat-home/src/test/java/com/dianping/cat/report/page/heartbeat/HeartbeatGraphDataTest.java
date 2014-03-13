package com.dianping.cat.report.page.heartbeat;

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
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.core.dal.Graph;
import com.dianping.cat.report.task.heartbeat.HeartbeatGraphCreator;

@RunWith(JUnit4.class)
public class HeartbeatGraphDataTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
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
		int size = (int) ((end.getTime() - start.getTime()) / (1000 * 3600));
		Map<String, double[]> result = handler.buildHeartbeatDatas(start, end, graphs);
		double[] ActiveThread = result.get("ActiveThread");
		double[] oneHourData = { 112.0, 112.0, 112.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0,
		      105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0,
		      105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 105.0, 106.0,
		      106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0, 106.0,
		      113.0, 113.0, 113.0 };
		double[] expectActiveThread = new double[size * 60];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < oneHourData.length; j++) {
				expectActiveThread[i * 60 + j] = oneHourData[j];
			}
		}
		Assert.assertEquals(true, Arrays.equals(ActiveThread, expectActiveThread));
	}

	private Graph creatGraph(Date start) throws IOException {
		Graph graph = new Graph();
		graph.setPeriod(start);
		graph.setDetailContent(getContent());
		return graph;
	}

	private String getContent() throws IOException {
		return Files.forIO().readFrom(getClass().getResourceAsStream("detail"), "utf-8");
	}

	@Test
	public void testModel() throws Exception {
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat.xml"), "utf-8");
		HeartbeatReport reportOld = DefaultSaxParser.parse(newXml);
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(reportOld.getDomain()));

		reportOld.accept(merger);
		Assert.assertEquals(newXml.replaceAll("\r", ""), reportOld.toString().replaceAll("\r", ""));
	}

	@Test
	public void test1() throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("heartbeat.xml"), "utf-8");
		HeartbeatReport report = DefaultSaxParser.parse(xml);
		HeartbeatGraphCreator creator = new HeartbeatGraphCreator();
		List<Graph> graphs = creator.splitReportToGraphs(report.getStartTime(), report.getDomain(), "heartbeat", report);
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("result"), "utf-8");
		
		Assert.assertEquals(result, graphs.get(0).getDetailContent());
	}
}
