package com.dianping.cat.report.page.system;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.system.graph.SystemReportConvertor;

public class SystemReportConvertorTest {

	@Test
	public void test() throws Exception {

		String metricReportXml = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport.xml"), "utf-8");
		String systemReportAllIpXml = Files.forIO().readFrom(getClass().getResourceAsStream("systemReportAllIp.xml"),
		      "utf-8");
		String systemReportIpFilterXml = Files.forIO().readFrom(
		      getClass().getResourceAsStream("systemReportIpFilter.xml"), "utf-8");

		MetricReport metricReport = DefaultSaxParser.parse(metricReportXml);
		SystemReportConvertor convert = new SystemReportConvertor("system", null);
		convert.visitMetricReport(metricReport);
		MetricReport systemReportAllIp = convert.getReport();

		Assert.assertEquals("Check all ip convert result!", systemReportAllIpXml.replace("\r", ""), systemReportAllIp
		      .toString().replace("\r", ""));

		Set<String> ipAddrs = new HashSet<String>(Arrays.asList("10.254.251.60"));
		SystemReportConvertor convertIpFilter = new SystemReportConvertor("system", ipAddrs);
		convertIpFilter.visitMetricReport(metricReport);
		MetricReport systemReportIpFilter = convertIpFilter.getReport();

		Assert.assertEquals("Check all ip convert result!", systemReportIpFilterXml.replace("\r", ""),
		      systemReportIpFilter.toString().replace("\r", ""));

	}
}
