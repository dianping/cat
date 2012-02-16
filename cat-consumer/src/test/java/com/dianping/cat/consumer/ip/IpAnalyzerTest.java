package com.dianping.cat.consumer.ip;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultXmlParser;
import com.site.helper.Files;

public class IpAnalyzerTest {
	@Test
	public void testXml() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("ip.xml"), "utf-8");
		IpReport report = parser.parse(expected);

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), report.toString().replace("\r", ""));
	}
}
