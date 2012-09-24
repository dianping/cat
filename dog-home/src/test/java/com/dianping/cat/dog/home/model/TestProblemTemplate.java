package com.dianping.cat.dog.home.model;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.dianping.cat.dog.home.template.model.entity.ThresholdTemplate;
import com.dianping.cat.dog.home.template.model.transform.DefaultDomParser;
import com.dianping.cat.dog.home.template.model.transform.DefaultXmlBuilder;
import com.site.helper.Files;

public class TestProblemTemplate {
	@Test
	public void testParseXml() throws IOException, SAXException{
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("threshold-template.xml"), "utf-8");
		ThresholdTemplate report = parser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(report);
		String expected = source;

		Assert.assertEquals("XML is not well parsed!", expected.replaceAll("\\s*", ""), xml.replaceAll("\\s*", ""));		
	}

}
