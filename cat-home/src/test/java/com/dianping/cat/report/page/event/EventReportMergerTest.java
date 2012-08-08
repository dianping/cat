package com.dianping.cat.report.page.event;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.model.event.EventReportMerger;

public class EventReportMergerTest {
	@Test
	public void testEventReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportNew.xml"), "utf-8");
		EventReport reportOld = new DefaultDomParser().parse(oldXml);
		EventReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportMergeResult.xml"),
		      "utf-8");
		EventReportMerger merger = new EventReportMerger(new EventReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), merger.getEventReport().toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""), reportNew.toString().replaceAll("\\s*", ""));
	}

	@Test
	public void testMergeAllIp() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportNew.xml"), "utf-8");
		EventReport reportOld = new DefaultDomParser().parse(oldXml);
		EventReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportMergeAllResult.xml"),
		      "utf-8");

		EventReportMerger merger = new EventReportMerger(new EventReport(reportOld.getDomain()));

		merger.setAllIp(true);

		reportOld.accept(merger);
		reportNew.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getEventReport());

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), actual.replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""), reportOld.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""), reportNew.toString().replaceAll("\\s*", ""));
	}

	@Test
	public void testMergeAllIpAllName() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportNew.xml"), "utf-8");
		EventReport reportOld = new DefaultDomParser().parse(oldXml);
		EventReport reportNew = new DefaultDomParser().parse(newXml);
		String expected = Files.forIO().readFrom(
		      getClass().getResourceAsStream("EventReportMergeAllIpAllName.xml"), "utf-8");

		EventReportMerger merger = new EventReportMerger(new EventReport(reportOld.getDomain()));

		merger.setAllIp(true);
		merger.setIp(CatString.ALL_IP);
		merger.setAllName(true);
		merger.setType("URL");

		reportOld.accept(merger);
		reportNew.accept(merger);

		String actual = new DefaultXmlBuilder().buildXml(merger.getEventReport());

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), actual.replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""), reportOld.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""), reportNew.toString().replaceAll("\\s*", ""));
	}
}
