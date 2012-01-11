package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.failure.FailureAnalyzerStoreTest;
import com.dianping.cat.consumer.failure.FailureAnalyzerTest;
import com.dianping.cat.consumer.transaction.TransactionReportMessageAnalyzerTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .impl */
ManyAnalyzerTest.class,

OneAnalyzerTwoDurationTest.class,

/* .model.failure */
FailureAnalyzerTest.class,
FailureAnalyzerStoreTest.class,

/* .transaction */
TransactionReportMessageAnalyzerTest.class

})
public class AllTests {

}
