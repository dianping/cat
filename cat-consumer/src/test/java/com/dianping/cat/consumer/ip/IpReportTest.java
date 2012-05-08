package com.dianping.cat.consumer.ip;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultJsonBuilder;
import com.dianping.cat.consumer.ip.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.ip.model.transform.DefaultDomParser;
import com.site.helper.Files;

public class IpReportTest {
	@Test
	public void testXml() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("IpReport.xml"), "utf-8");
		IpReport report = parser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(report);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}

	@Test
	public void testJson() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("IpReport.xml"), "utf-8");
		IpReport report = parser.parse(source);
		String json = new DefaultJsonBuilder().buildJson(report);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("IpReport.json"), "utf-8");

		Assert.assertEquals("XML is not well parsed or JSON is not well built!", expected.replace("\r", ""),
		      json.replace("\r", ""));
	}

}
