package com.dianping.cat.report.page.dependency;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.transform.DefaultJsonBuilder;
import com.dianping.cat.home.dependency.graph.transform.DefaultSaxParser;

public class DependencyGraphReport {

	@Test
	public void test() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("graph.xml"), "utf-8");
		TopologyGraph front = DefaultSaxParser.parse(oldXml);
		DefaultJsonBuilder build = new DefaultJsonBuilder();

		System.out.println(build.build(front));
	}
}
