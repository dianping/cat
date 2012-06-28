package com.dianping.cat.report.task;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.model.event.EventReportMerger;

public class HistoryEventMergerTest {
	@Test
	public void testMerge() throws Exception{
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("event.xml"), "utf-8");
		EventReport report1 = new DefaultDomParser().parse(oldXml);
		EventReport report2 = new DefaultDomParser().parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("eventResult.xml"),
		      "utf-8");
		EventReportMerger merger = new HistoryEventReportMerger(new EventReport(report1.getDomain()));

		report1.accept(merger);
		report2.accept(merger);
		
		String actual = new DefaultXmlBuilder().buildXml(merger.getEventReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));

	}
}
