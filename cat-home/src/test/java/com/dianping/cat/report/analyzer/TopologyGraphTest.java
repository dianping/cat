package com.dianping.cat.report.analyzer;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.metric.ProductLineConfigManager;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;

public class TopologyGraphTest extends ComponentTestCase{
	
	@Test
	public void test() throws ParseException{
		TopologyGraphManager manager = lookup(TopologyGraphManager.class);
		String date = "2014-07-06 18:00";
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		TopologyGraph graph = manager.queryGraphFromDB(formate.parse(date).getTime());
		
		System.out.println(graph);
	}
	
	@Test
	public void test1(){
		ProductLineConfigManager manager = lookup(ProductLineConfigManager.class);
		
		System.out.println(manager.getCompany());
	}
}
