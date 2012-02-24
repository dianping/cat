package com.dianping.cat.consumer.transaction;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultJsonBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.site.helper.Files;

public class TransactionReportTest {
	/**
	 * <range minute="5" count="123" sum="123456" avg="22.2" transactions="3"/>
	 * <range minute="10" count="123" sum="12457" avg="222" transactions="3"/>
	 * <duration value="128" count="34"/> <duration value="256" count="12"/>
	 */

	@Test
	public void testXml() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReport.xml"), "utf-8");
		TransactionReport report = parser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(report);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}

	@Test
	public void testJson() throws Exception {
		DefaultXmlParser parser = new DefaultXmlParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReport.xml"), "utf-8");
		TransactionReport report = parser.parse(source);
		String json = new DefaultJsonBuilder().buildJson(report);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReport.json"), "utf-8");

		Assert.assertEquals("XML is not well parsed or JSON is not well built!", expected.replace("\r", ""),
		      json.replace("\r", ""));
	}

}
