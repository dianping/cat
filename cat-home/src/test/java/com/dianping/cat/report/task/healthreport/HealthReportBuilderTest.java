package com.dianping.cat.report.task.healthreport;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.task.health.HealthReportCreator;

public class HealthReportBuilderTest {

	@Test
	public void testBuildHealthReport() throws Exception {
		HealthReportCreator builder = new HealthReportCreator();
		TransactionReport transactionReport = getTranscationReportFromFile("TransactionReport.xml");
		EventReport eventReport = getEventReportFromFile("EventReport.xml");
		ProblemReport problemReport = getProblemReportFromFile("ProblemReport.xml");

		HealthReport real = builder.build(transactionReport, eventReport, problemReport, null, null);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("HealthReport.xml"), "utf-8");

		Assert.assertEquals(expected.replaceAll("\\s*", ""), real.toString().replaceAll("\\s*", ""));
	}

	private TransactionReport getTranscationReportFromFile(String fileName) throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream(fileName), "utf-8");
		return new com.dianping.cat.consumer.transaction.model.transform.DefaultDomParser().parse(xml);

	}

	private EventReport getEventReportFromFile(String fileName) throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream(fileName), "utf-8");
		return new DefaultDomParser().parse(xml);

	}

	private ProblemReport getProblemReportFromFile(String fileName) throws Exception {
		String xml = Files.forIO().readFrom(getClass().getResourceAsStream(fileName), "utf-8");
		return new com.dianping.cat.consumer.problem.model.transform.DefaultDomParser().parse(xml);

	}
}
