package com.dianping.cat.report.page.event;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.report.page.model.event.EventReportMerger;

public class EventReportMergerTest {
	@Test
	public void testEventReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportNew.xml"), "utf-8");
		EventReport reportOld = new DefaultDomParser().parse(oldXml);
		EventReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportMergeResult.xml"), "utf-8");
		EventReportMerger merger = new EventReportMerger(new EventReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), merger.getEventReport()
		      .toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""),
		      reportNew.toString().replaceAll("\\s*", ""));
	}
}
