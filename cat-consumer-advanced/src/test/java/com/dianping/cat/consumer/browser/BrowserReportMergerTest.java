package com.dianping.cat.consumer.browser;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.consumer.browser.model.transform.DefaultSaxParser;

public class BrowserReportMergerTest extends ComponentTestCase {
	@Test
	public void mergeDomainTest() throws Exception{
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("new.xml"), "utf-8");
		BrowserReport reportOld = DefaultSaxParser.parse(oldXml);
		BrowserReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("result.xml"), "utf-8");
		BrowserReport result = DefaultSaxParser.parse(expected);

		BrowserReportMerger merger = new BrowserReportMerger(new BrowserReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", result.toString(), merger.getBrowserReport().toString());
	}

}
