package com.dianping.cat.report.page.dependency.graph;

import junit.framework.Assert;

import org.junit.Test;

public class TopologyGraphBuilderTest {

	@Test
	public void test() {
		TopologyGraphBuilder builder = new TopologyGraphBuilder();

		String result = builder.mergeDes("test", "test");
		String result2 = builder.mergeDes("test|test", "test");

		Assert.assertEquals("test", result);
		Assert.assertEquals("test|test", result2);
	}
}
