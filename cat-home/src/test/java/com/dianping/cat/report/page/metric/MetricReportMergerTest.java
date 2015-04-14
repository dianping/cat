package com.dianping.cat.report.page.metric;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.metric.service.MetricReportMerger;

public class MetricReportMergerTest {
	
	@Test
	public void testMetricReportMerge() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("MetricReportOld.xml"), "utf-8");
		String newXml = Files.forIO().readFrom(getClass().getResourceAsStream("MetricReportNew.xml"), "utf-8");
		MetricReport reportOld = DefaultSaxParser.parse(oldXml);
		MetricReport reportNew = DefaultSaxParser.parse(newXml);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("MetricReportMergeResult.xml"), "utf-8");
		MetricReportMerger merger = new MetricReportMerger(new MetricReport(reportOld.getProduct()));

		reportOld.accept(merger);
		reportNew.accept(merger);

		Assert.assertEquals("Check the merge result!", expected.replaceAll("\\r", ""), merger.getMetricReport().toString().replaceAll("\\r", ""));
		Assert.assertEquals("Source report is changed!", newXml.replaceAll("\\r", ""), reportNew.toString().replaceAll("\\r", ""));
	}

}
