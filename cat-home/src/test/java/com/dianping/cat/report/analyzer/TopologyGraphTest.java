package com.dianping.cat.report.analyzer;

import java.io.File;
import java.text.SimpleDateFormat;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.report.page.dependency.graph.TopologyGraphManager;

public class TopologyGraphTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		build("2014-07-06 18:00");
		build("2014-07-06 18:01");
		build("2014-07-06 18:02");
		build("2014-07-06 18:03");
		build("2014-07-06 18:04");
		build("2014-07-06 18:05");
		build("2014-07-06 18:06");
		build("2014-07-06 18:07");
		build("2014-07-06 18:08");
		build("2014-07-06 18:09");
	}

	public void build(String date) throws Exception {
		TopologyGraphManager manager = lookup(TopologyGraphManager.class);
		SimpleDateFormat formate = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		try {
	      TopologyGraph graph = manager.queryGraphFromDB(formate.parse(date).getTime());

	      if (graph != null) {
	      	File file = new File("/tmp/" + date + ".txt");

	      	if (!file.exists()) {
	      		file.createNewFile();
	      	}
	      	Files.forIO().writeTo(file, graph.toString());
	      }else{
	      	System.err.println(date+" is null1");
	      }
      } catch (Exception e) {
      	System.err.println(date+" is null1");
      }
	}

}
