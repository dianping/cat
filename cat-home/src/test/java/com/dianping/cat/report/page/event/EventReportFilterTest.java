package com.dianping.cat.report.page.event;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.event.service.LocalEventService.EventReportFilter;

public class EventReportFilterTest {
	@Test
	public void test() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("event_filter.xml"), "utf-8");
		EventReport report = DefaultSaxParser.parse(source);

		EventReportFilter f1 = new EventReportFilter(null, null, null);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("event_filter_type.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\r", ""), f1.buildXml(report).replaceAll("\r", ""));

		EventReportFilter f2 = new EventReportFilter("URL", null, null);
		String expected2 = Files.forIO().readFrom(getClass().getResourceAsStream("event_filter_name.xml"), "utf-8");

		Assert.assertEquals(expected2.replaceAll("\r", ""), f2.buildXml(report).replaceAll("\r", ""));
	}
}
