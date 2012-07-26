package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.graph.ValueTranslaterTest;
import com.dianping.cat.report.page.event.EventGraphDataTest;
import com.dianping.cat.report.page.heartbeat.HeartbeatGraphDataTest;
import com.dianping.cat.report.page.ip.DisplayModelTest;
import com.dianping.cat.report.page.model.EventReportFilterTest;
import com.dianping.cat.report.page.model.TransactionReportFilterTest;
import com.dianping.cat.report.page.model.event.EventModelServiceTest;
import com.dianping.cat.report.page.model.transaction.TransactionModelServiceTest;
import com.dianping.cat.report.page.problem.ProblemGraphDataTest;
import com.dianping.cat.report.page.problem.ProblemReportMergerTest;
import com.dianping.cat.report.page.sql.TestComputeStr;
import com.dianping.cat.report.page.transaction.PayloadTest;
import com.dianping.cat.report.page.transaction.TransactionGraphDataTest;
import com.dianping.cat.report.page.transaction.TransactionReportMergerTest;
import com.dianping.cat.report.task.EventDailyGraphMergerTest;
import com.dianping.cat.report.task.EventGraphCreatorTest;
import com.dianping.cat.report.task.HistoryEventMergerTest;
import com.dianping.cat.report.task.HistoryTransactionMergerTest;
import com.dianping.cat.report.task.ProblemCreateGraphDataTest;
import com.dianping.cat.report.task.ProblemDailyGraphMergerTest;
import com.dianping.cat.report.task.ProblemGraphCreatorTest;
import com.dianping.cat.report.task.TaskHelperTest;
import com.dianping.cat.report.task.TransactionDailyGraphMergerTest;
import com.dianping.cat.report.task.TransactionGraphCreatorTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.page.model.event */
EventModelServiceTest.class, EventGraphDataTest.class,

/* .report.page.model.transaction */
TransactionModelServiceTest.class,

/* .report.page.transaction */
TransactionReportMergerTest.class,

/* .report.graph */
ValueTranslaterTest.class,

/* .report.page.ip */
DisplayModelTest.class,

/* .report.page.heartbeart */
HeartbeatGraphDataTest.class,

/* .report.page.model */
EventReportFilterTest.class, TransactionReportFilterTest.class,

/* .report.page.sql */
TestComputeStr.class,

/* . report.page.transcation */
PayloadTest.class, TransactionReportMergerTest.class,

/* graph test */
EventGraphDataTest.class, HeartbeatGraphDataTest.class,

ProblemGraphDataTest.class, TransactionGraphDataTest.class,

ProblemReportMergerTest.class,

/* .report.task */
//TaskConsumerTest.class, 
TaskHelperTest.class,

HistoryEventMergerTest.class, HistoryTransactionMergerTest.class,

ProblemCreateGraphDataTest.class, ProblemGraphCreatorTest.class,

TransactionGraphCreatorTest.class, EventGraphCreatorTest.class, EventDailyGraphMergerTest.class,

TransactionDailyGraphMergerTest.class,ProblemDailyGraphMergerTest.class})
public class AllTests {
}
