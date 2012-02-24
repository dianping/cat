package com.dianping.cat.consumer.failure;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.transform.DefaultJsonBuilder;
import com.dianping.cat.consumer.failure.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.failure.model.transform.DefaultXmlParser;
import com.site.helper.Files;

public class FailureReportTest {
	@Test
	public void testXml() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("FailureReport.xml"), "utf-8");
		FailureReport report = parser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(report);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}

	@Test
	public void testJson() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("FailureReport.xml"), "utf-8");
		FailureReport report = parser.parse(source);
		String json = new DefaultJsonBuilder().buildJson(report);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("FailureReport.json"), "utf-8");

		Assert.assertEquals("XML is not well parsed or JSON is not well built!", expected.replace("\r", ""),
		      json.replace("\r", ""));
	}

}
