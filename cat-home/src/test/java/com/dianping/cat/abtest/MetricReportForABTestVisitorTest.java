package com.dianping.cat.abtest;

import java.io.IOException;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;
import org.xml.sax.SAXException;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.abtest.entity.AbtestReport;
import com.dianping.cat.report.task.abtest.MetricReportForABTestVisitor;

public class MetricReportForABTestVisitorTest {

	@Test
	public void test() throws IOException, SAXException {
		MetricReport metricReport = null;
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("merge_metric_report.xml"), "utf-8");
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport.xml"), "utf-8");
		metricReport = DefaultSaxParser.parse(xml);

		if (metricReport != null) {
			MetricReportForABTestVisitor visitor = new MetricReportForABTestVisitor();

			metricReport.accept(visitor);

			Map<Integer, AbtestReport> result = visitor.getReportMap();

			StringBuilder sb = new StringBuilder(1024);
			for (AbtestReport ar : result.values()) {
				sb.append(ar.toString());
			}

			Assert.assertEquals(sb.toString().replaceAll("\r", ""), expected.replaceAll("\r", ""));
		}
	}
}
