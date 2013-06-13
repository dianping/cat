package com.dianping.cat.consumer.core;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;

public class ProblemReportTest {
	@Test
	public void testXml() throws Exception {
		String source = Files.forIO().readFrom(getClass().getResourceAsStream("problem-report.xml"), "utf-8");
		ProblemReport root = DefaultSaxParser.parse(source);
		String xml = new DefaultXmlBuilder().buildXml(root);
		String expected = source;

		Assert.assertEquals(expected.replace("\r", ""), xml.replace("\r", ""));

		// byte[] data = DefaultNativeBuilder.build(root);
		// ProblemReport report = DefaultNativeParser.parse(data);

		// Assert.assertEquals(root.toString(), report.toString());
	}
}
