package com.dianping.cat.report.page.dependency;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.home.dependency.entity.DependencyGraph;
import com.dianping.cat.home.dependency.transform.DefaultJsonBuilder;
import com.dianping.cat.home.dependency.transform.DefaultSaxParser;

public class DependencyGraphReport {

	@Test
	public void test() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("graph.xml"), "utf-8");
		DependencyGraph front = DefaultSaxParser.parse(oldXml);
		DefaultJsonBuilder build = new DefaultJsonBuilder();

		System.out.println(build.buildJson(front));
	}
}
