package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.core.FormatTest;
import com.dianping.cat.consumer.core.GsonTest;
import com.dianping.cat.consumer.core.NumberFormatTest;
import com.dianping.cat.consumer.core.ProblemHandlerTest;
import com.dianping.cat.consumer.core.TransactionAnalyzerTest;
import com.dianping.cat.consumer.core.TransactionReportFilterTest;
import com.dianping.cat.consumer.core.TransactionReportMessageAnalyzerTest;
import com.dianping.cat.consumer.core.TransactionReportTest;

@RunWith(Suite.class)
@SuiteClasses({

PeriodStrategyTest.class,

ManyAnalyzerTest.class,

OneAnalyzerTwoDurationTest.class,

ProblemHandlerTest.class,

FormatTest.class,

GsonTest.class,

NumberFormatTest.class,

TransactionAnalyzerTest.class,

TransactionReportMessageAnalyzerTest.class,

TransactionReportTest.class,

TransactionReportFilterTest.class

})
public class AllTests {

}
