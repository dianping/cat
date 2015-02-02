package com.dianping.cat.consumer.event;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;

public class EventReportMergerTest {
	@Test
	public void testEventReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("event_report_old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("event_report_new.xml"), "utf-8");
		EventReport reportOld = DefaultSaxParser.parse(oldXml);
		EventReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("event_report_mergeResult.xml"), "utf-8");
		EventReportMerger merger = new EventReportMerger(new EventReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""), merger.getEventReport()
		      .toString().replaceAll("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\r", ""),
		      reportNew.toString().replaceAll("\r", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\r", ""),
				      reportOld.toString().replaceAll("\r", ""));
	}
}
