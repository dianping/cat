package com.dianping.cat.report.page.model.event;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.site.helper.Files;

public class EventNameAggregatorTest {
	@Test
	public void test() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("event.xml"), "utf-8");
		EventReport report = parser.parse(source);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("event-names.xml"), "utf-8");
		EventName all = new EventNameAggregator(report).mergesFor("URL");

		Assert.assertEquals(expected.replace("\r", ""), all.toString().replace("\r", ""));
	}
}
