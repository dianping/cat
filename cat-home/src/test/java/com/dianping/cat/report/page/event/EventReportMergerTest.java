package com.dianping.cat.report.page.event;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.report.task.event.HistoryEventReportMerger;

public class EventReportMergerTest {

	@Test
	public void test() throws Exception{
		String xml1 = Files.forIO().readFrom(getClass().getResourceAsStream("event1.xml"), "utf-8");
		EventReport report1 = new DefaultDomParser().parse(xml1);
		
		String xml2 = Files.forIO().readFrom(getClass().getResourceAsStream("event2.xml"), "utf-8");
		EventReport report2 = new DefaultDomParser().parse(xml2);
		
		HistoryEventReportMerger merger = new HistoryEventReportMerger(new EventReport(report1.getDomain()));

		report1.accept(merger);
		report2.accept(merger);
		
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("MergerResult.xml"), "utf-8");
		
		Assert.assertEquals(result.replace("\r", ""), merger.getEventReport().toString().replace("\r", ""));
	}
}
