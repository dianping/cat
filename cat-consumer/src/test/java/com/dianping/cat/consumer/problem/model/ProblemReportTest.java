package com.dianping.cat.consumer.problem.model;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultJsonBuilder;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import org.unidal.helper.Files;

public class ProblemReportTest {
	@Test
	public void testXml() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report.xml"), "utf-8");
		ProblemReport root = parser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(root);
		String expected = source;

		Assert.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		// byte[] data = DefaultNativeBuilder.build(root);
		// ProblemReport report = DefaultNativeParser.parse(data);

		// Assert.assertEquals(root.toString(), report.toString());
	}

	@Test
	public void testJson() throws Exception {
		DefaultDomParser parser = new DefaultDomParser();
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report.xml"), "utf-8");
		ProblemReport root = parser.parse(source);
		String json = new DefaultJsonBuilder().buildJson(root);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report.json"), "utf-8");

		Assert.assertEquals("XML is not well parsed or JSON is not well built!", expected.replace("\r", ""),
		      json.replace("\r", ""));
	}

}
