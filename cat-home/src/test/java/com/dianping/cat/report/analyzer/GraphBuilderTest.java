package com.dianping.cat.report.analyzer;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.report.task.event.EventReportBuilder;
import com.dianping.cat.report.task.heartbeat.HeartbeatReportBuilder;
import com.dianping.cat.report.task.problem.ProblemReportBuilder;
import com.dianping.cat.report.task.state.HistoryStateReportMerger;
import com.dianping.cat.report.task.state.StateReportBuilder.ClearDetailInfo;
import com.dianping.cat.report.task.transaction.TransactionReportBuilder;
import com.dianping.cat.report.task.utilization.UtilizationReportBuilder;

public class GraphBuilderTest extends ComponentTestCase {

	@Test
	public void test() throws Exception {
		HeartbeatReportBuilder builder = lookup(HeartbeatReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-18 10:00:00");
		builder.buildHourlyTask(HeartbeatAnalyzer.ID, "ReviewWeb", period);
	}

	@Test
	public void testProblem() throws Exception {
		ProblemReportBuilder builder = lookup(ProblemReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(ProblemAnalyzer.ID, "Cat", period);
	}

	@Test
	public void testTransaction() throws Exception {
		TransactionReportBuilder builder = lookup(TransactionReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(TransactionAnalyzer.ID, "Cat", period);
	}

	@Test
	public void testEvent() throws Exception {
		EventReportBuilder builder = lookup(EventReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(EventAnalyzer.ID, "Cat", period);
	}

	@Test
	public void testUtilization() throws Exception {
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-24 10:00:00");
		builder.buildHourlyTask(Constants.REPORT_UTILIZATION, "Cat", period);
	}

	@Test
	public void testStateReportBuilder() throws Exception {
		ReportServiceManager service = lookup(ReportServiceManager.class);
		Date date = TimeUtil.getCurrentMonth();
		long start = date.getTime();
		long end = System.currentTimeMillis();
		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport("Cat"));

		for (; start < end; start = start + TimeUtil.ONE_DAY) {
			StateReport stateReport = service.queryStateReport("Cat", new Date(start), new Date(start + TimeUtil.ONE_DAY));

			stateReport.accept(merger);
		}
		StateReport report = merger.getStateReport();
		new ClearDetailInfo().visitStateReport(report);
	}
}
