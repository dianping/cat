package com.dianping.cat.system.notify;

import junit.framework.Assert;

import org.junit.Test;
import org.unidal.helper.Files;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;

public class RenderTest extends ComponentTestCase {
	
	@Test
	public void testTransaction() throws Exception {
		ReportRender render = lookup(ReportRender.class);
		String excepted = Files.forIO().readFrom(getClass().getResourceAsStream("TransactionRender.txt"), "utf-8");
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("Transaction.xml"), "utf-8");
		TransactionReport report = DefaultSaxParser.parse(oldXml);

		String result = render.renderReport(report);
		System.out.println(result);
		Assert.assertEquals(excepted.replaceAll("\r", ""), result.replaceAll("\r", ""));
		System.out.println("---------");
	}

	@Test
	public void testEvent() throws Exception {
		ReportRender render = lookup(ReportRender.class);
		String excepted = Files.forIO().readFrom(getClass().getResourceAsStream("EventRender.txt"), "utf-8");
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("Event.xml"), "utf-8");
		EventReport report = com.dianping.cat.consumer.event.model.transform.DefaultSaxParser.parse(oldXml);

		String result = render.renderReport(report);
		System.out.println(result);
		Assert.assertEquals(excepted.replaceAll("\r", ""), result.replaceAll("\r", ""));
		System.out.println("--------");
	}
	
	@Test
	public void testProblem() throws Exception {
		ReportRender render = lookup(ReportRender.class);
		String excepted = Files.forIO().readFrom(getClass().getResourceAsStream("ProblemRender.txt"), "utf-8");
		String oldXml = Files.forIO().readFrom(getClass().getResourceAsStream("Problem.xml"), "utf-8");
		ProblemReport report = com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser.parse(oldXml);

		String result = render.renderReport(report);
		System.out.println(result);
		Assert.assertEquals(excepted.replaceAll("\r", ""), result.replaceAll("\r", ""));
		System.out.println("---------");
	}
	
}
