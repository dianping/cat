package com.dianping.cat.report.task.event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;

public class BuilderTest extends ComponentTestCase {
	
	@Test
	public void test() throws ParseException{
		HeartbeatReportBuilder builder = lookup(HeartbeatReportBuilder.class);
		
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(HeartbeatAnalyzer.ID, "Cat", period);
	}

	
	@Test
	public void testProblem() throws ParseException{
		ProblemReportBuilder builder = lookup(ProblemReportBuilder.class);
		
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(ProblemAnalyzer.ID, "Cat", period);
	}
	
	@Test
	public void testTransaction() throws ParseException{
		TransactionReportBuilder builder = lookup(TransactionReportBuilder.class);
		
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(TransactionAnalyzer.ID, "Cat", period);
	}
	

	@Test
	public void testEvent() throws ParseException{
		EventReportBuilder builder = lookup(EventReportBuilder.class);
		
		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(EventAnalyzer.ID, "Cat", period);
	}
}
