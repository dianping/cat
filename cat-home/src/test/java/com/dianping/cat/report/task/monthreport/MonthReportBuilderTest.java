package com.dianping.cat.report.task.monthreport;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.webres.helper.Files;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultDomParser;
import com.dianping.cat.consumer.monthreport.model.entity.MonthReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class MonthReportBuilderTest {

	@Test
	public void testCreatLastMonthFirstDay() {
		MonthReportBuilderTask task = new MonthReportBuilderTask();
		Date date = task.getMonthFirstDay(0);
		System.out.println(date);

		date = task.getMonthFirstDay(-1);
		System.out.println(date);
		date = task.getMonthFirstDay(-2);
		System.out.println(date);
	}

	@Test
	public void testBuildMonthReport() throws Exception {
		MonthReportBuilder builder= new MonthReportBuilder();
		TransactionReport transactionReport = getTranscationReportFromFile("TransactionReport.xml");
		EventReport eventReport =getEventReportFromFile("EventReport.xml");
		ProblemReport problemReport =getProblemReportFromFile("ProblemReport.xml");
		
		MonthReport real = builder.build(transactionReport, eventReport, problemReport);
		String expected = Files.forIO().readFrom(getClass().getResourceAsStream("MonthReport.xml"), "utf-8");
		
		Assert.assertEquals(expected.replaceAll("\\s*", ""),real.toString().replaceAll("\\s*", ""));
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
