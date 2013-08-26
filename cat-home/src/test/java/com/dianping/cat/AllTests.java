package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.baseline.BaselineCreatorTest;
import com.dianping.cat.report.graph.ValueTranslaterTest;
import com.dianping.cat.report.page.cross.CMDBTest;
import com.dianping.cat.report.page.cross.CrossReportMergerTest;
import com.dianping.cat.report.page.dependency.DependencyReportMergerTest;
import com.dianping.cat.report.page.dependency.graph.GraphConfigManagerTest;
import com.dianping.cat.report.page.event.EventGraphDataTest;
import com.dianping.cat.report.page.heartbeat.HeartbeatGraphDataTest;
import com.dianping.cat.report.page.jsError.ParseTest;
import com.dianping.cat.report.page.metric.MetricReportMergerTest;
import com.dianping.cat.report.page.metric.MetricReportParseTest;
import com.dianping.cat.report.page.model.EventReportFilterTest;
import com.dianping.cat.report.page.model.TransactionReportFilterTest;
import com.dianping.cat.report.page.problem.ProblemGraphDataTest;
import com.dianping.cat.report.page.problem.ProblemReportMergerTest;
import com.dianping.cat.report.page.sql.SqlReportMergerTest;
import com.dianping.cat.report.page.state.StateReportMergerTest;
import com.dianping.cat.report.page.transaction.PayloadTest;
import com.dianping.cat.report.page.transaction.TransactionGraphDataTest;
import com.dianping.cat.report.page.transaction.TransactionReportMergerTest;
import com.dianping.cat.report.task.TaskConsumerTest;
import com.dianping.cat.report.task.TaskHelperTest;
import com.dianping.cat.report.task.event.EventDailyGraphMergerTest;
import com.dianping.cat.report.task.event.EventGraphCreatorTest;
import com.dianping.cat.report.task.event.HistoryEventMergerTest;
import com.dianping.cat.report.task.heavy.HeavyReportBuilderTest;
import com.dianping.cat.report.task.problem.ProblemCreateGraphDataTest;
import com.dianping.cat.report.task.problem.ProblemDailyGraphMergerTest;
import com.dianping.cat.report.task.problem.ProblemDailyGraphTest;
import com.dianping.cat.report.task.problem.ProblemGraphCreatorTest;
import com.dianping.cat.report.task.service.ServiceReportMergerTest;
import com.dianping.cat.report.task.transaction.DailyTransactionReportGraphTest;
import com.dianping.cat.report.task.transaction.HistoryTransactionMergerTest;
import com.dianping.cat.report.task.transaction.TransactionDailyGraphMergerTest;
import com.dianping.cat.report.task.transaction.TransactionGraphCreatorTest;
import com.dianping.cat.system.alarm.template.TemplateMergerTest;
import com.dianping.cat.system.alarm.template.ThresholdRuleTest;
import com.dianping.cat.system.notify.RenderTest;

@RunWith(Suite.class)
@SuiteClasses({
/*
 * TestHttp.class
 */

/* .report.graph */
ValueTranslaterTest.class,

/* .report.page.model */
EventReportFilterTest.class, TransactionReportFilterTest.class,

/* . report.page.transcation */
PayloadTest.class, TransactionReportMergerTest.class,

/* . report.page.cross */
CrossReportMergerTest.class,

/* graph test */
EventGraphDataTest.class, HeartbeatGraphDataTest.class,

ProblemGraphDataTest.class, TransactionGraphDataTest.class,

ProblemReportMergerTest.class,

/* sql test */
SqlReportMergerTest.class,

/* .report.task */
TaskConsumerTest.class, TaskHelperTest.class,

HistoryEventMergerTest.class, HistoryTransactionMergerTest.class,

ProblemCreateGraphDataTest.class, ProblemGraphCreatorTest.class,

TransactionGraphCreatorTest.class, EventGraphCreatorTest.class, EventDailyGraphMergerTest.class,

TransactionDailyGraphMergerTest.class, ProblemDailyGraphMergerTest.class,

/* alarm .render */
RenderTest.class, ThresholdRuleTest.class, TemplateMergerTest.class,

StateReportMergerTest.class,

/* Daily Graph Test */
DailyTransactionReportGraphTest.class, ProblemDailyGraphTest.class,

/* Metric */
MetricReportParseTest.class, MetricReportMergerTest.class,

/* Dependency */
DependencyReportMergerTest.class, GraphConfigManagerTest.class,

/* CMDB */
CMDBTest.class,

/* BaseLine */
BaselineCreatorTest.class,

MetricReportParseTest.class,

/* jsError */
ParseTest.class,

/* service */
ServiceReportMergerTest.class,

HeavyReportBuilderTest.class })
public class AllTests {
}
