package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.ip.IpReportTest;
import com.dianping.cat.consumer.problem.model.ProblemReportTest;
import com.dianping.cat.consumer.transaction.FormatTest;
import com.dianping.cat.consumer.transaction.GsonTest;
import com.dianping.cat.consumer.transaction.NumberFormatTest;
import com.dianping.cat.consumer.transaction.TransactionAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportMessageAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .ip */
IpReportTest.class,

TransactionAnalyzerTest.class,

TransactionReportMessageAnalyzerTest.class,

TransactionReportTest.class,

ManyAnalyzerTest.class,

OneAnalyzerTwoDurationTest.class,

ProblemReportTest.class,

FormatTest.class, GsonTest.class,

NumberFormatTest.class,

})
public class AllTests {

}
