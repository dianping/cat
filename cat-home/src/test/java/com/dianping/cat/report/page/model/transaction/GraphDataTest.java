package com.dianping.cat.report.page.model.transaction;

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
import com.dianping.cat.report.page.transaction.Handler;
import com.site.helper.Files;

@RunWith(JUnit4.class)
public class GraphDataTest {
	
	@Test
	public void testBuildGraphDatasByType() throws IOException{
	
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
		Map<String, double[]> graphDatas=handler.buildGraphDates(start, end, "URL", "", graphs);
		double[] total_count=graphDatas.get("total_count");
		double[]failure_count=graphDatas.get("failure_count");
		double[] sum = graphDatas.get("sum");
		double[] avg = new double[sum.length];
		for (int i = 0; i < avg.length; i++) {
	      avg[i]=sum[i] / total_count[i];
      }
		assertArray(2,total_count);
		assertArray(0,failure_count);
		assertArray((double)21.9/(double)2,avg);
	}
	
	@Test
	public void testBuildGraphDatasByTypeAndName() throws IOException{
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
		Map<String, double[]> graphDatas=handler.buildGraphDates(start, end, "Task", "Status", graphs);
		double[] total_count=graphDatas.get("total_count");
		double[]failure_count=graphDatas.get("failure_count");
		double[] sum = graphDatas.get("sum");
		double[] avg = new double[sum.length];
		for (int i = 0; i < avg.length; i++) {
	      avg[i]=sum[i] / total_count[i];
      }
		assertArray(59,total_count);
		assertArray(0,failure_count);
		assertArray((double)3257.7/(double)59,avg);
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
