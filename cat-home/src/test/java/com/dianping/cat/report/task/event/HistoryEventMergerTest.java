package com.dianping.cat.report.task.event;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.event.task.HistoryEventReportMerger;

public class HistoryEventMergerTest {
	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryBaseEvent.xml"), "utf-8");
		EventReport report1 = DefaultSaxParser.parse(oldXml);
		EventReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("HistoryEventMergerDaily.xml"), "utf-8");
		EventReportMerger merger = new HistoryEventReportMerger(new EventReport(report1.getDomain()));

		report1.accept(merger);
		report2.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getEventReport());

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}

}
