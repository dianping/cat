package com.dianping.cat;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.dianping.cat.report.page.ip.DisplayModelTest;
import com.dianping.cat.report.tool.FailureReportToolTest;
import com.dianping.cat.report.tool.IpReportToolTest;
import com.dianping.cat.report.tool.TransactionReportToolTest;

@RunWith(Suite.class)
@SuiteClasses({

/* .report.page.ip */
DisplayModelTest.class,

/* report.tool*/
FailureReportToolTest.class,

IpReportToolTest.class,

TransactionReportToolTest.class

})
public class AllTests {

}
