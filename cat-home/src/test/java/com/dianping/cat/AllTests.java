package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.page.ip.DisplayModelTest;
import com.dianping.cat.report.page.transaction.TransactionReportMergerTest;
import com.dianping.cat.report.tool.FailureReportToolTest;
import com.dianping.cat.report.tool.IpReportToolTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.page.ip */
DisplayModelTest.class,

/* report.tool*/
FailureReportToolTest.class,

IpReportToolTest.class,

TransactionReportMergerTest.class

})
public class AllTests {

}
