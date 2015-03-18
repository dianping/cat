package com.dianping.cat.report.page.web;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.web.graph.WebReportConvertor;

public class WebReportConvertorTest {

	public MetricReport hackForTest(MetricReport report, Map<String, String> properties) {
		String city = properties.get("city");
		String channel = properties.get("channel");
		String type = properties.get("type");
		WebReportConvertor convert = new WebReportConvertor(type, city, channel);

		convert.visitMetricReport(report);
		return convert.getReport();
	}

	@Test
	public void test() throws Exception {

		String reportXml = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport.xml"), "utf-8");
		MetricReport report = DefaultSaxParser.parse(reportXml);
		report.setProduct("userMonitor");
		Map<String, String> prop = new HashMap<String, String>();

		prop.put("city", "江苏-南京");
		prop.put("channel", "中国移动");
		prop.put("type", "info");

		MetricReport reportInfo = hackForTest(report, prop);
		String userMonitorReportInfo = Files.forIO().readFrom(
		      getClass().getResourceAsStream("userMonitorReportInfo.xml"), "utf-8");

		Assert.assertEquals("Check info info convert result!", userMonitorReportInfo.replace("\r", ""), reportInfo
		      .toString().replace("\r", ""));

		prop.put("city", "江苏-南京");
		prop.put("channel", "中国联通");
		prop.put("type", "httpStatus");

		MetricReport reportHttpStatus = hackForTest(report, prop);
		String userMonitorReportHttpStatus = Files.forIO().readFrom(
		      getClass().getResourceAsStream("userMonitorReportHttpStatus.xml"), "utf-8");

		Assert.assertEquals("Check http status convert result!", userMonitorReportHttpStatus.replace("\r", ""),
		      reportHttpStatus.toString().replace("\r", ""));

		prop.put("city", "江苏-扬州");
		prop.put("channel", "中国联通");
		prop.put("type", "errorCode");

		MetricReport reportErrorCode = hackForTest(report, prop);
		String userMonitorReportErrorCode = Files.forIO().readFrom(
		      getClass().getResourceAsStream("userMonitorReportErrorCode.xml"), "utf-8");

		Assert.assertEquals("Check error code convert result!", userMonitorReportErrorCode.replace("\r", ""),
		      reportErrorCode.toString().replace("\r", ""));
	}
}
