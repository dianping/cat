package com.dianping.cat.consumer.state;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

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

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\r", ""), merger.getStateReport()
		      .toString().replaceAll("\r", ""));
	}
}
