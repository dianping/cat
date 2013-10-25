package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.core.FormatTest;
import com.dianping.cat.consumer.core.GsonTest;
import com.dianping.cat.consumer.core.NumberFormatTest;
import com.dianping.cat.consumer.core.ProblemHandlerTest;
import com.dianping.cat.consumer.core.ProblemReportAggregationTest;
import com.dianping.cat.consumer.core.aggregation.CompositeFormatTest;
import com.dianping.cat.consumer.core.aggregation.DefaultFormatTest;
import com.dianping.cat.consumer.event.EventAnalyzerTest;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportFilterTest;
import com.dianping.cat.consumer.transaction.TransactionReportTest;

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

/* event */
EventAnalyzerTest.class,

/* heartbeat */
HeartbeatAnalyzerTest.class,

CompositeFormatTest.class,

DefaultFormatTest.class,

ProblemReportAggregationTest.class

})
public class AllTests {

}
