package com.dianping.cat.report.page.model;

import junit.framework.Assert;

import org.junit.Test;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.report.page.model.Handler.EventReportFilter;
import org.unidal.helper.Files;

public class EventReportFilterTest {
	@Test
	public void test() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("event.xml"), "utf-8");
		EventReport report = parser.parse(source);

		EventReportFilter f1 = new EventReportFilter(null, null, null);
		String expected1 = Files.forIO().readFrom(getClass().getResourceAsStream("event-type.xml"), "utf-8");

		Assert.assertEquals(expected1.replaceAll("\r", ""), f1.buildXml(report).replaceAll("\r", ""));

		EventReportFilter f2 = new EventReportFilter("URL", null, null);
		String expected2 = Files.forIO().readFrom(getClass().getResourceAsStream("event-name.xml"), "utf-8");

		Assert.assertEquals(expected2.replaceAll("\r", ""), f2.buildXml(report).replaceAll("\r", ""));
	}
}
