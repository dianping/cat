package com.dianping.cat.consumer.dependency;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;

public class DependencyReportMergerTest {
	@Test
	public void testDependencyReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("dependency_new.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("dependency_new.xml"), "utf-8");
		DependencyReport reportOld = DefaultSaxParser.parse(oldXml);
		DependencyReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("dependency_analyzer_merger.xml"), "utf-8");
		DependencyReportMerger merger = new DependencyReportMerger(new DependencyReport(reportOld.getDomain()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replace("\r", ""), merger.getDependencyReport().toString()
		      .replace("\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replace("\r", ""), reportNew.toString().replace("\r", ""));
	}
}
