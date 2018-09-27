package com.dianping.cat.report.page.server;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.xml.sax.SAXException;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;
import com.dianping.cat.report.page.server.service.MetricGraphService;

public class GraphServiceTest extends ComponentTestCase {

	@Test
	public void test() throws IOException, SAXException {
		MetricGraphService graphService = lookup(MetricGraphService.class);

		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("graph.xml"), "utf-8");
		Graph graph = DefaultSaxParser.parse(xml);
		String id = graph.getId();

		graphService.insert(graph);

		Graph graph2 = graphService.queryByGraphId(Long.valueOf(id));

		Assert.assertEquals(graph, graph2);

	}
}
