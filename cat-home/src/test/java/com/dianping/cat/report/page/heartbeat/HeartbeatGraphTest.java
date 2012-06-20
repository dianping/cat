package com.dianping.cat.report.page.heartbeat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import com.dianping.cat.hadoop.dal.Graph;
import com.site.helper.Files;
import com.site.lookup.ComponentTestCase;

@RunWith(JUnit4.class)
public class HeartbeatGraphTest extends ComponentTestCase {
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
		Map<String, double[]> result = handler.buildHeartbeatDates(start, end, graphs);

		System.out.println(result.size());
		for (Entry<String, double[]> entry : result.entrySet()) {
			System.out.print(entry.getKey() + " : ");
			double sum = 0;
			double[] value = entry.getValue();
			for (int i = 0; i < value.length; i++) {
				sum += value[i];
				System.out.print(value[i] + " ");
			}
			System.out.println();
			if (sum != 14400 && sum != 0) {
				sum = sum * 1024 * 1024;
			}

			System.out.println("sum:" + sum);
		}
	}

	private Graph creatGraph(Date start) throws IOException {
		Graph graph = new Graph();
		graph.setCreationDate(start);
		graph.setDomain("Test");
		graph.setEndDate(new Date(start.getTime() + 3600 * 1000));
		graph.setIp("192.168.32.68");
		graph.setName("heartbeat");
		graph.setPeriod(start);
		graph.setDetailContent(getContent());
		return graph;
	}

	private String getContent() throws IOException {
		return Files.forIO().readFrom(getClass().getResourceAsStream("detail"), "utf-8");
	}
}
