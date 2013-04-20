package com.dianping.cat.consumer.transaction;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultJsonBuilder;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;

public class TransactionReportTest {
	@Test
	public void testXml() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReport.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(report);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}

	@Test
	public void testJson() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReport.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(source);
		String json = new DefaultJsonBuilder().buildJson(report);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionReport.json"), "utf-8");

		Assert.assertEquals("XML is not well parsed or JSON is not well built!", expected.replace("\r", ""),
		      json.replace("\r", ""));
	}
}
