package com.dianping.cat.report.page.dependency;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;

public class DependencyReportMergerTest {
	@Test
	public void testDependencyReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("old.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("new.xml"), "utf-8");
		DependencyReport reportOld = DefaultSaxParser.parse(oldXml);
		DependencyReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("result.xml"), "utf-8");
		DependencyReport result = DefaultSaxParser.parse(expected);

		DependencyReportMerger merger = new DependencyReportMerger(new DependencyReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", result.toString(), merger.getDependencyReport().toString());
	}

}
