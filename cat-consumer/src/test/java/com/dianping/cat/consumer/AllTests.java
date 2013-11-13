package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.core.FormatTest;
import com.dianping.cat.consumer.core.GsonTest;
import com.dianping.cat.consumer.core.NumberFormatTest;
import com.dianping.cat.consumer.core.aggregation.CompositeFormatTest;
import com.dianping.cat.consumer.core.aggregation.DefaultFormatTest;
import com.dianping.cat.consumer.event.EventAnalyzerTest;
import com.dianping.cat.consumer.event.EventReportMergerTest;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzerTest;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMergerTest;
import com.dianping.cat.consumer.problem.ProblemAnalyzerTest;
import com.dianping.cat.consumer.problem.ProblemHandlerTest;
import com.dianping.cat.consumer.problem.ProblemReportAggregationTest;
import com.dianping.cat.consumer.problem.ProblemReportMergerTest;
import com.dianping.cat.consumer.problem.ProblemReportTest;
import com.dianping.cat.consumer.state.StateAnalyzerTest;
import com.dianping.cat.consumer.state.StateReportMergerTest;
import com.dianping.cat.consumer.top.TopAnalyzerTest;
import com.dianping.cat.consumer.top.TopReportMergerTest;
import com.dianping.cat.consumer.transaction.TransactionAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportFilterTest;
import com.dianping.cat.consumer.transaction.TransactionReportMergerTest;
import com.dianping.cat.consumer.transaction.TransactionReportTest;
import com.dianping.cat.consumer.transaction.TransactionReportTypeAggergatorTest;

@RunWith(Suite.class)
@SuiteClasses({

PeriodStrategyTest.class,

ProblemHandlerTest.class,

FormatTest.class,

GsonTest.class,

NumberFormatTest.class,

/* transaction */

TransactionAnalyzerTest.class,

TransactionReportTest.class,

TransactionReportFilterTest.class,

TransactionReportMergerTest.class,

/* event */
EventAnalyzerTest.class,

EventReportMergerTest.class,

/* heartbeat */
HeartbeatAnalyzerTest.class,

HeartbeatReportMergerTest.class,

/* state */
StateAnalyzerTest.class,

StateReportMergerTest.class,

/* top */
TopAnalyzerTest.class,

TopReportMergerTest.class,

/* problem */
ProblemReportAggregationTest.class,

ProblemHandlerTest.class,

ProblemReportTest.class,

ProblemAnalyzerTest.class,

ProblemReportMergerTest.class,

CompositeFormatTest.class,

DefaultFormatTest.class,

TransactionReportTypeAggergatorTest.class
})
public class AllTests {

}
