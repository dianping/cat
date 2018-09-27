package com.dianping.cat.report.task.event;

import java.text.SimpleDateFormat;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.event.model.transform.DefaultXmlBuilder;
import com.dianping.cat.report.page.event.task.EventReportDailyGraphCreator;
import com.dianping.cat.report.page.event.task.EventReportHourlyGraphCreator;

public class EventGraphCreatorTest {

	@Test
	public void testMergeHourlyGraph() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("BaseEventGraphReport.xml"),
		      "utf-8");
		EventReport report1 = DefaultSaxParser.parse(oldXml);
		EventReport report2 = DefaultSaxParser.parse(oldXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("EventReportHourlyGraphResult.xml"), "utf-8");

		EventReport result = new EventReport(report1.getDomain());

		EventReportHourlyGraphCreator creator = new EventReportHourlyGraphCreator(result, 10);

		creator.createGraph(report1);
		creator.createGraph(report2);

		String actual = new DefaultXmlBuilder().buildXml(result);
		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}

	@Test
	public void testMergeDailyGraph() throws Exception {
		String oldXml1 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyEventReport1.xml"),
		      "utf-8");
		String oldXml2 = Files.forIO().readFrom(getClass().getResourceAsStream("BaseDailyEventReport2.xml"),
		      "utf-8");

		EventReport report1 = DefaultSaxParser.parse(oldXml1);
		EventReport report2 = DefaultSaxParser.parse(oldXml2);
		String expected = Files.forIO().readFrom(
		      getClass().getResourceAsStream("EventReportDailyGraphResult.xml"), "utf-8");

		EventReport result = new EventReport(report1.getDomain());

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		EventReportDailyGraphCreator creator = new EventReportDailyGraphCreator(result, 7, sdf.parse("2016-01-23 00:00:00"));

		creator.createGraph(report1);
		creator.createGraph(report2);

		String actual = new DefaultXmlBuilder().buildXml(result);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), actual.replace("\r", ""));
	}
}
