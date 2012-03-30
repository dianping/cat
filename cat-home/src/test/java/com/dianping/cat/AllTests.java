package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.page.ip.DisplayModelTest;
import com.dianping.cat.report.page.model.EventReportFilterTest;
import com.dianping.cat.report.page.model.TransactionReportFilterTest;
import com.dianping.cat.report.page.transaction.TransactionReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.page.ip */
DisplayModelTest.class,

/* .report.page.model */
EventReportFilterTest.class,

TransactionReportFilterTest.class,

/* .report.page.transaction */
TransactionReportMergerTest.class

})
public class AllTests {

}
