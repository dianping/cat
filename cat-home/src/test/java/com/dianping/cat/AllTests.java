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
import com.dianping.cat.report.page.event.EventReportFilterTest;
import com.dianping.cat.report.page.event.EventTrendGraphBuilderTest;
import com.dianping.cat.report.page.problem.ProblemReportMergerTest;
import com.dianping.cat.report.page.problem.ProblemTrendGraphBuilderTest;
import com.dianping.cat.report.page.state.StateReportMergerTest;
import com.dianping.cat.report.page.transaction.PayloadTest;
import com.dianping.cat.report.page.transaction.TransactionReportFilterTest;
import com.dianping.cat.report.page.transaction.TransactionTrendGraphBuilderTest;
import com.dianping.cat.report.task.TaskConsumerTest;
import com.dianping.cat.report.task.TaskHelperTest;
import com.dianping.cat.report.task.event.EventGraphCreatorTest;
import com.dianping.cat.report.task.event.HistoryEventMergerTest;
import com.dianping.cat.report.task.heartbeat.HeartbeatDailyMergerTest;
import com.dianping.cat.report.task.heavy.HeavyReportBuilderTest;
import com.dianping.cat.report.task.metric.AlertConfigTest;
import com.dianping.cat.report.task.problem.ProblemReportDailyGraphCreatorTest;
import com.dianping.cat.report.task.problem.ProblemReportHourlyGraphCreatorTest;
import com.dianping.cat.report.task.service.ServiceReportMergerTest;
import com.dianping.cat.report.task.storage.HistoryStorageReportMergerTest;
import com.dianping.cat.report.task.transaction.HistoryTransactionMergerTest;
import com.dianping.cat.report.task.transaction.TransactionReportGraphCreatorTest;

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

/* .report.task */
TaskConsumerTest.class,

TaskHelperTest.class,

HistoryEventMergerTest.class,

HistoryTransactionMergerTest.class,

ProblemReportHourlyGraphCreatorTest.class,

ProblemReportDailyGraphCreatorTest.class,

TransactionReportGraphCreatorTest.class,

EventGraphCreatorTest.class,

StateReportMergerTest.class,

/* Graph */
EventTrendGraphBuilderTest.class,

ProblemTrendGraphBuilderTest.class,

TransactionTrendGraphBuilderTest.class,

/* service */
ServiceReportMergerTest.class,

HistoryStorageReportMergerTest.class,

AlertConfigTest.class,

HeavyReportBuilderTest.class,

RuleConfigTest.class,

AlertConfigTest.class,

HeartbeatDailyMergerTest.class,

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
