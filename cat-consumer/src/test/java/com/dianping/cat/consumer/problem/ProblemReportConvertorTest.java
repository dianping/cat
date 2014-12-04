package com.dianping.cat.consumer.problem;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;

public class ProblemReportConvertorTest {

	@Test
	public void test() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("problemReportForConvert.xml"), "utf-8");
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("problemReportConvertResult.xml"), "utf-8");
		ProblemReport report = DefaultSaxParser.parse(oldXml);
		ProblemReportConvertor convertor = new ProblemReportConvertor();
		report.accept(convertor);
		Assert.assertEquals("Source report is changed!", result.replace("\r", ""), report.toString().replace("\r", ""));

	}

}
