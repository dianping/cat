package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.page.ip.DisplayModelTest;
import com.dianping.cat.report.page.ip.location.IPSeekerTest;
import com.dianping.cat.report.page.model.EventReportFilterTest;
import com.dianping.cat.report.page.model.TransactionReportFilterTest;
import com.dianping.cat.report.page.model.event.EventModelServiceTest;
import com.dianping.cat.report.page.model.event.EventNameAggregatorTest;
import com.dianping.cat.report.page.model.transaction.TransactionModelServiceTest;
import com.dianping.cat.report.page.model.transaction.TransactionNameAggregatorTest;
import com.dianping.cat.report.page.transaction.TransactionReportMergerTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.page.ip */
DisplayModelTest.class,

/* .report.page.model */
EventReportFilterTest.class,

TransactionReportFilterTest.class,

/* .report.page.model.event */
EventModelServiceTest.class,

EventNameAggregatorTest.class,

/* .report.page.model.transaction */
TransactionModelServiceTest.class,

TransactionNameAggregatorTest.class,

/* .report.page.transaction */
TransactionReportMergerTest.class,

IPSeekerTest.class

})
public class AllTests {

}
