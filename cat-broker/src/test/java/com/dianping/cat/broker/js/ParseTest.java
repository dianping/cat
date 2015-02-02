package com.dianping.cat.broker.js;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.broker.api.page.js.Handler;

public class ParseTest {

	@Test
	public void test() {
		Handler analyzer = new Handler();
		String data = "aaa=1.1&a=1.1&abc=2.2&c=1.2&aaaa=11.1&abbbb=1.1&ssabc=2.2&sc=1.2";
		for (int i = 0; i < 1000; i++) {
			analyzer.parseValue("abc", data);
		}

		Assert.assertEquals(analyzer.parseValue("aaa", "aaa=1.1"), "1.1");
		Assert.assertEquals(analyzer.parseValue("aaa", data), "1.1");
		Assert.assertEquals(analyzer.parseValue("a", data), "1.1");
		Assert.assertEquals(analyzer.parseValue("abc", data), "2.2");
		Assert.assertEquals(analyzer.parseValue("c", data), "1.2");
		Assert.assertEquals(analyzer.parseValue("aaaa", data), "11.1");
		Assert.assertEquals(analyzer.parseValue("abbbb", data), "1.1");
		Assert.assertEquals(analyzer.parseValue("ssabc", data), "2.2");
		Assert.assertEquals(analyzer.parseValue("sc", data), "1.2");
	}

}
