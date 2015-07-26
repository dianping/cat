package com.dianping.cat;

import java.util.Locale;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.alert.ExtractDataTest;
import com.dianping.cat.report.alert.JudgeTimeTest;
import com.dianping.cat.report.alert.MetricIdAndRuleMappingTest;
import com.dianping.cat.report.alert.RuleConfigTest;
import com.dianping.cat.report.graph.ValueTranslaterTest;
import com.dianping.cat.report.page.cross.CrossReportMergerTest;
import com.dianping.cat.report.page.database.DatabaseReportFilterTest;
import com.dianping.cat.report.page.dependency.DependencyReportMergerTest;
import com.dianping.cat.report.page.event.EventGraphDataTest;
import com.dianping.cat.report.page.event.EventReportFilterTest;
import com.dianping.cat.report.page.metric.MetricReportMergerTest;
import com.dianping.cat.report.page.metric.MetricReportParseTest;
import com.dianping.cat.report.page.problem.ProblemGraphDataTest;
import com.dianping.cat.report.page.problem.ProblemReportMergerTest;
import com.dianping.cat.report.page.state.StateReportMergerTest;
import com.dianping.cat.report.page.system.SystemReportConvertorTest;
import com.dianping.cat.report.page.transaction.PayloadTest;
import com.dianping.cat.report.page.transaction.TransactionGraphDataTest;
import com.dianping.cat.report.page.transaction.TransactionReportFilterTest;
import com.dianping.cat.report.page.web.WebReportConvertorTest;
import com.dianping.cat.report.task.TaskConsumerTest;
import com.dianping.cat.report.task.TaskHelperTest;
import com.dianping.cat.report.task.event.EventDailyGraphMergerTest;
import com.dianping.cat.report.task.event.EventGraphCreatorTest;
import com.dianping.cat.report.task.event.HistoryEventMergerTest;
import com.dianping.cat.report.task.heartbeat.HeartbeatDailyMergerTest;
import com.dianping.cat.report.task.heavy.HeavyReportBuilderTest;
import com.dianping.cat.report.task.metric.AlertConfigTest;
import com.dianping.cat.report.task.problem.ProblemCreateGraphDataTest;
import com.dianping.cat.report.task.problem.ProblemDailyGraphMergerTest;
import com.dianping.cat.report.task.problem.ProblemDailyGraphTest;
import com.dianping.cat.report.task.problem.ProblemGraphCreatorTest;
import com.dianping.cat.report.task.service.ServiceReportMergerTest;
import com.dianping.cat.report.task.storage.HistoryStorageReportMergerTest;
import com.dianping.cat.report.task.system.SystemReportStatisticsTest;
import com.dianping.cat.report.task.transaction.DailyTransactionReportGraphTest;
import com.dianping.cat.report.task.transaction.HistoryTransactionMergerTest;
import com.dianping.cat.report.task.transaction.TransactionDailyGraphMergerTest;
import com.dianping.cat.report.task.transaction.TransactionGraphCreatorTest;
import com.dianping.cat.system.notify.RenderTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.graph */
ValueTranslaterTest.class,

/* .report.page.model */
EventReportFilterTest.class,

TransactionReportFilterTest.class,

ProblemReportMergerTest.class,

/* . report.page.transcation */
PayloadTest.class,

/* . report.page.cross */
CrossReportMergerTest.class,

/* graph test */
EventGraphDataTest.class,

ProblemGraphDataTest.class,

TransactionGraphDataTest.class,

/* .report.task */
TaskConsumerTest.class,

TaskHelperTest.class,

HistoryEventMergerTest.class,

HistoryTransactionMergerTest.class,

ProblemCreateGraphDataTest.class,

ProblemGraphCreatorTest.class,

TransactionGraphCreatorTest.class,

EventGraphCreatorTest.class,

EventDailyGraphMergerTest.class,

TransactionDailyGraphMergerTest.class,

ProblemDailyGraphMergerTest.class,

/* alarm .render */
RenderTest.class,

StateReportMergerTest.class,

/* Daily Graph Test */
DailyTransactionReportGraphTest.class,

ProblemDailyGraphTest.class,

/* Metric */
MetricReportParseTest.class,

MetricReportMergerTest.class,

/* Dependency */
DependencyReportMergerTest.class,

MetricReportParseTest.class,

/* service */
ServiceReportMergerTest.class,

HistoryStorageReportMergerTest.class,

AlertConfigTest.class,

HeavyReportBuilderTest.class,

RuleConfigTest.class,

AlertConfigTest.class,

SystemReportConvertorTest.class,

DatabaseReportFilterTest.class,

HeartbeatDailyMergerTest.class,

WebReportConvertorTest.class,

SystemReportStatisticsTest.class,

MetricIdAndRuleMappingTest.class,

ExtractDataTest.class,

JudgeTimeTest.class })
public class AllTests {

	@BeforeClass
	public static void setUp() {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
		Locale.setDefault(Locale.CHINESE);
	}
}
