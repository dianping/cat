package com.dianping.cat.report.page.metric;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.metric.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;

public class MetricReportParseTest {

	@Test
	public void test() throws Exception {
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("metric-report.xml"), "utf-8");
		MetricReport report = DefaultSaxParser.parse(oldXml);
		byte[] bytes = DefaultNativeBuilder.build(report);

		MetricReport report2 = DefaultNativeParser.parse(bytes);

		Assert.assertEquals(report.toString(), report2.toString());
	}
}
