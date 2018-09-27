package com.dianping.cat.report.task.service;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.helper.Files;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.statistics.task.service.ClientReportStatistics;

public class ClientReportStatisticsTest {

	@Test
	public void test() throws Exception {
		ClientReportStatistics statistics = new ClientReportStatistics();
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream("transactionReport.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(xml);
		String xml2 = Files.forIO().readFrom(getClass().getResourceAsStream("transactionReport2.xml"), "utf-8");
		TransactionReport report2 = DefaultSaxParser.parse(xml2);
		String result = Files.forIO().readFrom(getClass().getResourceAsStream("clientReport.xml"), "utf-8");

		report.accept(statistics);
		report2.accept(statistics);
		Assert.assertEquals("Check the build result!", result.replace("\r", ""), statistics.getClienReport().toString()
		      .replace("\r", ""));
	}
}
