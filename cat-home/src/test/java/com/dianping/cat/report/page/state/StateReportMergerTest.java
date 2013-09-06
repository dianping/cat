package com.dianping.cat.report.page.state;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.state.StateReportMerger;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultSaxParser;

public class StateReportMergerTest {
	@Test
	public void testStateReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("new.xml"), "utf-8");
		StateReport reportOld = DefaultSaxParser.parse(oldXml);
		StateReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO()
		      .readFrom(getClass().getResourceAsStream("result.xml"), "utf-8");
		
		StateReportMerger merger = new StateReportMerger(new StateReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\s*", ""), merger.getStateReport()
		      .toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\s*", ""),
		      reportNew.toString().replaceAll("\\s*", ""));
		Assert.assertEquals("Source report is changed!", oldXml.replaceAll("\\s*", ""),
		      reportOld.toString().replaceAll("\\s*", ""));
	}

}
