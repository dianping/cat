package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.transaction.DailyTransactionReportGraphTest;
import com.dianping.cat.transaction.HistoryTransactionMergerTest;
import com.dianping.cat.transaction.PayloadTest;
import com.dianping.cat.transaction.TransactionAnalyzerTest;
import com.dianping.cat.transaction.TransactionDailyGraphMergerTest;
import com.dianping.cat.transaction.TransactionGraphCreatorTest;
import com.dianping.cat.transaction.TransactionGraphDataTest;
import com.dianping.cat.transaction.TransactionReportCountFilterTest;
import com.dianping.cat.transaction.TransactionReportFilterTest;
import com.dianping.cat.transaction.TransactionReportMergerTest;
import com.dianping.cat.transaction.TransactionReportTest;
import com.dianping.cat.transaction.TransactionReportTypeAggergatorTest;

@RunWith(Suite.class)
@SuiteClasses({

      /* transaction */

      TransactionAnalyzerTest.class,

      TransactionReportTest.class,

      TransactionReportCountFilterTest.class,

      TransactionReportMergerTest.class,

      TransactionReportTypeAggergatorTest.class,

      PayloadTest.class,

      TransactionReportFilterTest.class,

      TransactionGraphDataTest.class,

      HistoryTransactionMergerTest.class,

      TransactionGraphCreatorTest.class,

      TransactionDailyGraphMergerTest.class,

      DailyTransactionReportGraphTest.class,

})
public class AllTests {

}
