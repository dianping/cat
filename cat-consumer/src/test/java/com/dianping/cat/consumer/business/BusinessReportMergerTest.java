package com.dianping.cat.consumer.business;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.transform.DefaultSaxParser;

public class BusinessReportMergerTest {

	@Test
	public void testBusinessReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("business_base.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("business_merger.xml"), "utf-8");
		BusinessReport reportOld = DefaultSaxParser.parse(oldXml);
		BusinessReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("business_merger_result.xml"),
		      "utf-8");
		BusinessReportMerger merger = new BusinessReportMerger(new BusinessReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getBusinessReport().toString()
		      .replace("\r", ""));
	}
}
