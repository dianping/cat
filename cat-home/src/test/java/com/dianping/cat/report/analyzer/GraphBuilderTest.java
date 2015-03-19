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
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.event.task.EventReportBuilder;
import com.dianping.cat.report.page.heartbeat.task.HeartbeatReportBuilder;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.state.task.HistoryStateReportMerger;
import com.dianping.cat.report.page.state.task.StateReportBuilder.ClearDetailInfo;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;

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
		builder.buildHourlyTask(ProblemAnalyzer.ID, "cat", period);
	}

	@Test
	public void testTransaction() throws Exception {
		TransactionReportBuilder builder = lookup(TransactionReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(TransactionAnalyzer.ID, "cat", period);
	}

	@Test
	public void testEvent() throws Exception {
		EventReportBuilder builder = lookup(EventReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-16 16:00:00");
		builder.buildHourlyTask(EventAnalyzer.ID, "cat", period);
	}

	@Test
	public void testUtilization() throws Exception {
		UtilizationReportBuilder builder = lookup(UtilizationReportBuilder.class);

		Date period = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2013-12-24 10:00:00");
		builder.buildHourlyTask(Constants.REPORT_UTILIZATION, "cat", period);
	}

	@Test
	public void testStateReportBuilder() throws Exception {
		StateReportService service = lookup(StateReportService.class);
		Date date = TimeHelper.getCurrentMonth();
		long start = date.getTime();
		long end = System.currentTimeMillis();
		HistoryStateReportMerger merger = new HistoryStateReportMerger(new StateReport("cat"));

		for (; start < end; start = start + TimeHelper.ONE_DAY) {
			StateReport stateReport = service.queryReport("cat", new Date(start), new Date(start + TimeHelper.ONE_DAY));

			stateReport.accept(merger);
		}
		StateReport report = merger.getStateReport();
		new ClearDetailInfo().visitStateReport(report);
	}
}
