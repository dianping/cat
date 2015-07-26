package com.dianping.cat.report.page.database;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.transform.DefaultSaxParser;

public class DatabaseReportFilterTest {

	@Test
	public void test() throws Exception {

		String report = Files.forIO().readFrom(getClass().getResourceAsStream("report.xml"), "utf-8");
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("expected.xml"), "utf-8");
		MetricReport metricReport = DefaultSaxParser.parse(report);

		DatabaseReportFilter filter = new DatabaseReportFilter(DatabaseGroup.KEY_GROUPS.get("InnoDB Info"));
		filter.visitMetricReport(metricReport);
		metricReport = filter.getReport();

		Assert.assertEquals("Check database report filter result!", expected.toString().replace("\r", ""), metricReport
		      .toString().replace("\r", ""));

	}
}
