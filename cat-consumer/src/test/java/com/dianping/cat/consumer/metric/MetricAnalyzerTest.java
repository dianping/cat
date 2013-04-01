package com.dianping.cat.consumer.metric;

import junit.framework.Assert;

import org.junit.Test;

public class MetricAnalyzerTest {

	@Test
	public void test() {
		MetricAnalyzer analyzer = new MetricAnalyzer();
		String data = "aaa=1.1&a=1.1&abc=2.2&c=1.2&aaaa=1.1&abbbb=1.1&ssabc=2.2&sc=1.2";

		long t = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			analyzer.parseValue("abc", data);
		}
		System.out.println(System.currentTimeMillis() - t);

		t = System.currentTimeMillis();
		for (int i = 0; i < 100000; i++) {
			analyzer.parseValue1("abc", data);
		}
		System.out.println(System.currentTimeMillis() - t);

		Assert.assertEquals(analyzer.parseValue("aaa", data), 1.1);
		Assert.assertEquals(analyzer.parseValue("a", data), 1.1);
		Assert.assertEquals(analyzer.parseValue("abc", data), 2.2);
		Assert.assertEquals(analyzer.parseValue("c", data), 1.2);
		Assert.assertEquals(analyzer.parseValue("aaaa", data), 1.1);
		Assert.assertEquals(analyzer.parseValue("abbbb", data), 1.1);
		Assert.assertEquals(analyzer.parseValue("ssabc", data), 2.2);
		Assert.assertEquals(analyzer.parseValue("sc", data), 1.2);
	}

}
