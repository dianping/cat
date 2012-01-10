package com.dianping.cat.consumer;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.consumer.impl.ManyAnalyzerTest;
import com.dianping.cat.consumer.impl.OneAnalyzerTwoDurationTest;
import com.dianping.cat.message.consumer.failure.FailureAnalyzerStoreTest;
import com.dianping.cat.message.consumer.failure.FailureAnalyzerTest;
import com.dianping.cat.message.consumer.transaction.TransactionReportMessageAnalyzerTest;

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
