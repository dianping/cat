package com.dianping.cat.report.task.system;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;
import com.dianping.cat.home.system.entity.SystemReport;
import com.dianping.cat.report.page.statistics.task.system.SystemReportStatistics;

public class SystemReportStatisticsTest {
	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Test
	public void test() throws Exception {
		SystemReport systemReport = new SystemReport();
		Date startTime = m_sdf.parse("2014-07-05 00:00:00");
		Date endTime = m_sdf.parse("2014-07-06 00:00:00");

		systemReport.setStartTime(startTime);
		systemReport.setEndTime(endTime);

		List<String> keys = Arrays.asList("sysCpu", "userCpu", "cpuUsage");
		SystemReportStatistics statistics = new SystemReportStatistics(startTime.getTime(), systemReport, keys);
		String xml1 = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport1.xml"), "utf-8");
		MetricReport report1 = DefaultSaxParser.parse(xml1);
		String xml2 = Files.forIO().readFrom(getClass().getResourceAsStream("metricReport2.xml"), "utf-8");
		MetricReport report2 = DefaultSaxParser.parse(xml2);
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("systemReport.xml"), "utf-8");

		report1.accept(statistics);
		report2.accept(statistics);
		Assert.assertEquals("Check the build result!", result.replace("\r", ""), systemReport.toString()
		      .replace("\r", ""));
	}
}
