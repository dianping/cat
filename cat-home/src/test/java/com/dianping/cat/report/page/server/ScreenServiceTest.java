package com.dianping.cat.report.page.server;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;
import org.xml.sax.SAXException;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;
import com.dianping.cat.report.page.server.display.MetricScreenInfo;
import com.dianping.cat.report.page.server.service.MetricScreenService;

public class ScreenServiceTest extends ComponentTestCase {

	@Test
	public void test() throws IOException, SAXException {
		MetricScreenService screenService = lookup(MetricScreenService.class);
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("graph.xml"), "utf-8");
		String xml2 = Files.forIO().readFrom(getClass().getResourceAsStream("graph2.xml"), "utf-8");
		Graph graph = DefaultSaxParser.parse(xml);
		Graph graph2 = DefaultSaxParser.parse(xml2);

//		screenService.insert("screen1"," graph);
//		screenService.insert("screen1", "system", graph2);

		MetricScreenInfo graph3 = screenService.queryByNameGraph("screen1", graph.getId());
		MetricScreenInfo graph4 = screenService.queryByNameGraph("screen1", graph2.getId());

		Assert.assertEquals(graph, graph3.getGraph());
		Assert.assertEquals(graph2, graph4.getGraph());

	}
}
