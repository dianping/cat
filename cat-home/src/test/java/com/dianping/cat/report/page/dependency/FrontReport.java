package com.dianping.cat.report.page.dependency;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.home.front.entity.Front;
import com.dianping.cat.home.front.transform.DefaultJsonBuilder;
import com.dianping.cat.home.front.transform.DefaultSaxParser;

public class FrontReport {

	@Test
	public void test() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("front.xml"), "utf-8");
		Front front = DefaultSaxParser.parse(oldXml);
		DefaultJsonBuilder build = new DefaultJsonBuilder();

		System.out.println(build.buildJson(front));
	}
}
