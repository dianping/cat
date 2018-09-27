package com.dianping.cat.report.page.server;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.home.graph.entity.Graph;
import com.dianping.cat.home.graph.transform.DefaultSaxParser;

public class GraphTests {

	@Test
	public void test() throws IOException, SAXException {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("graph.xml"), "utf-8");
		Graph graph = DefaultSaxParser.parse(xml);

		Assert.assertEquals("Check the result!", xml.replaceAll("\r", ""), graph.toString().replaceAll("\r", ""));

	}

}
