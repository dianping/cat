package com.dianping.cat.notify.report;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.dianping.cat.notify.BaseTest;
import com.dianping.cat.notify.model.entity.ScheduleReports;
import com.dianping.cat.notify.model.transform.DefaultDomParser;
import com.dianping.cat.notify.model.transform.DefaultXmlBuilder;
import com.site.helper.Files;

public class ScheduleReportTest extends BaseTest {
	
	@Test
	public void testParseXml() throws IOException, SAXException{
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("schedule-report.xml"), "utf-8");
		ScheduleReports report = parser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(report);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));		
	}

}
