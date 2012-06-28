package com.dianping.cat.report.page.heartbeat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.hadoop.dal.Graph;
import com.site.helper.Files;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HeartbeatGraphDataTest extends ComponentTestCase {
	@Test
	public void test() throws Exception {
		Handler handler = new Handler();
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
		Map<String, double[]> result = handler.buildHeartbeatDatas(start, end, graphs);
		double[] ActiveThread=result.get("ActiveThread");
		assertArray(10,ActiveThread);
	}
	
	public void assertArray(double expected, double[] real) {
		for (int i = 0; i < real.length; i++) {
			Assert.assertEquals(expected, real[i]);
		}
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
}
