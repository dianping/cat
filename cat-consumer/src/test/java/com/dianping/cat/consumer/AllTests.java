package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.cross.ParseCrossInfoTest;
import com.dianping.cat.consumer.database.DatabaseAnalyzerTest;
import com.dianping.cat.consumer.ip.IpReportTest;
import com.dianping.cat.consumer.matrix.MatrixReportFilterTest;
import com.dianping.cat.consumer.transaction.FormatTest;
import com.dianping.cat.consumer.transaction.GsonTest;
import com.dianping.cat.consumer.transaction.NumberFormatTest;
import com.dianping.cat.consumer.transaction.TransactionAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportFilterTest;
import com.dianping.cat.consumer.transaction.TransactionReportMessageAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportTest;

@RunWith(Suite.class)
@SuiteClasses({

//OneAnalyzerTwoDurationTest.class,

//ProblemReportTest.class,

/* .ip */
IpReportTest.class,

TransactionAnalyzerTest.class,

TransactionReportMessageAnalyzerTest.class,

TransactionReportTest.class,

//ManyAnalyzerTest.class,

FormatTest.class, GsonTest.class,

NumberFormatTest.class,

MatrixReportFilterTest.class,

TransactionReportFilterTest.class,

/* cross analyzer */
ParseCrossInfoTest.class,

DatabaseAnalyzerTest.class})
public class AllTests {

}
