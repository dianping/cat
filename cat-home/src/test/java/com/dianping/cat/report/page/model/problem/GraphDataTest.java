package com.dianping.cat.report.page.model.problem;

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

import com.dianping.cat.hadoop.dal.Graph;
import com.dianping.cat.report.page.problem.Handler;
import com.site.helper.Files;

@RunWith(JUnit4.class)
public class GraphDataTest {
	
	public static final long ONE_HOUR = 3600 * 1000L;
	
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
		Map<String, double[]> graphDatas=handler.buildGraphDates(start, end, "heartbeat", "", graphs);
		double[] errors=graphDatas.get("errors");
		double[]expectErrors=new double[errors.length];
		for (int i = 0; i < expectErrors.length; i++) {
			expectErrors[i]=1;
      }
		Assert.assertEquals(true,Arrays.equals(errors, expectErrors));
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
		Map<String, double[]> graphDatas=handler.buildGraphDates(start, end, "long-url", "/addcheckin.bin", graphs);
		double[] errors=graphDatas.get("errors");
		double[]expectErrors=new double[errors.length];
		for(int i=2;i<expectErrors.length;i=i+60){
			expectErrors[i]=1;
		}
		Assert.assertEquals(true,Arrays.equals(errors, expectErrors));
	}
	
	private String getContent(String fileName) throws IOException {
		String s=Files.forIO().readFrom(getClass().getResourceAsStream(fileName), "utf-8");
		return  s;
	}
	
	private Graph creatGraph(Date period) throws IOException {
		Graph graph = new Graph();
		graph.setPeriod(period);
		String detail=getContent("detail");
		graph.setDetailContent(detail);
		graph.setSummaryContent(getContent("summary"));
		return graph;
	}
}
