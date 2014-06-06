package com.dianping.cat.report.task.alert;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.alert.report.transform.DefaultSaxParser;
import com.dianping.cat.report.task.alert.exception.AlertReportMerger;

public class AlertReportBuilderTest  extends ComponentTestCase {
	
	@Test
	public void testMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("new.xml"), "utf-8");
		AlertReport reportOld = DefaultSaxParser.parse(oldXml);
		AlertReport reportNew = DefaultSaxParser.parse(newXml);
		
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("result.xml"), "utf-8");
		AlertReportMerger merger = new AlertReportMerger(new AlertReport(reportOld.getDomain()));
		
		reportOld.accept(merger);
		reportNew.accept(merger);
		
		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getAlertReport().toString()
		      .replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));


	}

}
