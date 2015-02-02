package com.dianping.cat.status.model;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.transform.DefaultSaxParser;
import com.dianping.cat.status.model.transform.DefaultXmlBuilder;

public class StatusInfoTest {
	@Test
	public void testXml() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("status.xml"), "utf-8");
		StatusInfo root = DefaultSaxParser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(root);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replace("\r", ""), xml.replace("\r", ""));
	}
}
